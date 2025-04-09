# hw4ctls-kt

Experiments with CIRCT: use infrastracture for modeling hardware in Kotlin.

Some source files are taken from  https://github.com/circt/circt and https://github.com/circt/arc-tests repositories.
- download latest `circt-full-` build: https://github.com/llvm/circt/releases

## Create Model

### With Chisel

- https://www.chisel-lang.org/docs/installation
- https://www.chisel-lang.org/docs/resources/faqs#get-me-firrtl

Install Scala CLI
- https://scala-cli.virtuslab.org/install/

In chisel dir:

Download Chisel sample project:
```sh
curl -O -L https://github.com/chipsalliance/chisel/releases/latest/download/chisel-example.scala
```

Add emit FIRRTL output stages:
```scala
object Main extends App {
  ChiselStage.emitCHIRRTLFile(
    new Foo,
    Array("--target-dir", "gen"),
  )
}
```

Build model:
```sh
scala-cli chisel-example.scala --main-class Main
```

Get MLIR for required dialect
```sh
# firrtl
firtool gen/Foo.fir --format=fir --parse-only -o gen/Foo.firrtl.mlir
firtool gen/Foo.fir --format=fir --ir-fir -o gen/Foo.firrtl.mlir

# hw
firtool gen/Foo.fir --format=fir --ir-hw -o gen/Foo.hw.mlir

# verilog
firtool gen/Foo.fir --format=fir --ir-sv -o gen/Foo.sv.mlir
firtool gen/Foo.fir --format=fir --ir-verilog -o gen/Foo.v.mlir
firtool gen/Foo.fir --format=fir --verilog -disable-all-randomization -strip-debug-info -o gen/Foo.v
```

clean:
```sh
rm gen/
```

For simple counter in `model.scala`:
```scala
class Dut extends Module {
  val count = IO(Output(UInt(8.W)))
  
  val counter = RegInit(0.U(8.W))
  count := counter

  counter := counter + 1.U
}
```

Build simple model
```sh
scala-cli model.scala --main-class Main
firtool gen/Dut.fir --format=fir --ir-hw -o gen/Dut.hw.mlir
```

and compile:
```sh
mkdir out
arcilator gen/Dut.hw.mlir --emit-llvm --observe-memories --observe-named-values --observe-ports --observe-registers --observe-wires --state-file=out/model-states.json -o out/model.ll
llc out/model.ll -O3 --filetype=obj -o out/model.o
llvm-objdump --disassemble out/model.o | save -f out/model.objdump
```
- now comparing `arc/out/model.objdump` and `chisel/out/model.objdump` says it is similar but not equal

## Build Model

on macos it's prefered to use docker with `--platform=amd64`:
```sh
docker run -it --name circt \
  -v ${pwd}:/mnt/circt \
  -v /path/to/CIRCT-release/linux/firtool-1.112.0:/mnt/circt-bin \ --platform=linux/amd64 \
  ubuntu bash
```
or use x86 version of JDK to load x86 dylib via Rosetta.

Build script inside `arc/`:
```sh
arcilator model.mlir --emit-llvm --observe-memories --observe-named-values --observe-ports --observe-registers --observe-wires --state-file=out/model-states.json -o out/model.ll
llc out/model.ll -O3 --filetype=obj -o out/model.o
python arcilator-header-cpp.py out/model-states.json --view-depth 1 | save -f out/model.h
clang++ model-main.cpp out/model.o -I. -Iout -o out/model-main.exe
./out/model-main.exe -o out/model-trace.vcd

llvm-objdump --disassemble out/model.o | save -f out/model.objdump
firtool model.mlir --verilog -o out/model.sv
```

Try to get MLIR from Verilog fails as yosys produces invalid firrtl:

```sh
Yosys\oss-cad-suite\start.bat
yosys -p "read_verilog -sv out/model.sv; write_firrtl out/model.fir"
firtool out/model.fir out/model.fir.mlir
```

Clean:
 ```sh
 rm out/*
 ```

Binary model sample output:
```console
dut.o = 20
total time = 1300
VCD have written to file 'out/model-trace.vcd'
```
> Total time is approximate and varies from 1000 to 3000 for 20 clock cycles.

First try to call model from Kotlin/Native (see MPP template sources).

### static lib

Creation static lib is similar on windows & linux:
```sh
llvm-ar rc out/model.a out/model.o
# copy model.a to src/mingwX64Main/kotlin/lib/
# copy model.a to src/linuxX64Main/kotlin/lib/
```

execute Kotlin application:
```sh
./gradlew.bat :library:mingwX64Binaries
./library/build/bin/mingwX64/releaseExecutable/library.exe
 # or
./gradlew.bat :library:linuxX64Binaries
./library/build/bin/linuxX64/releaseExecutable/library.kexe
```

prints:
```console
Hello Native world!
11
```

### dynamic lib


#### Raw FFM API

Using linker for properly compilation

get object file with model, it's better to compile with PIC:
```sh
llc out/model.ll -O3 --filetype=obj -relocation-model=pic -o out/model.o
```
complile to dynamic library:

on linux needs just shared library:
```sh
clang -shared -o out/libmodel.so out/model.o
# copy libmodel.so to src/jvmMain/lib/
```
- and set up `LD_LIBRARY_PATH` on load with JVM, `java.library.path` is not enough!

on windows it requires to export symbols:
```sh
clang -shared -o out/model.dll out/model.o
lld-link /DLL /NOENTRY /DEF:model.def /out:out/model.dll out/model.o
# or single command
clang -shared -o out/model.dll out/model.o -Wl,/DEF:model.def
# copy model.dll to src/jvmMain/lib/
```
- and set up `java.library.path`.
- `model.def`:
  ```def
  LIBRARY model
  EXPORTS
      Dut_eval
  ```
  - or manually use linker `/EXPORT:Dut_eval` option to export specific function.

on macos:
```sh
# nushell
clang -arch x86_64 -isysroot (xcrun --sdk macosx --show-sdk-path) -nostartfiles -nodefaultlibs -dynamiclib out/model.o -lSystem -o out/libmodel.dylib
# copy libmodel.dylib to src/jvmMain/lib/
```
- only x86 jdk can load such library, so set JAVA_HOME to MacOS x64 version of JDK (https://jdk.java.net/archive/)

#### FFM API with Jextract

- https://jdk.java.net/jextract/
- https://github.com/openjdk/jextract/blob/master/samples/
- https://github.com/whyoleg/kotlin-interop-playground/blob/main/build-logic/src/main/kotlin/kipbuild.jextract.gradle.kts

Use simple `dut.h` (take from cinterop)

```sh
jextract --output ../java -t io.github.e1turin.cirkt -lmodel dut.h
```
- add `LD_LIBRARY_PATH` or `java.library.path` as for raw API!
- add `withJava()` to kotlin config to use generated java source set


execute Kotlin application:
```sh
./gradlew :library:jvmRun
# or
./gradlew.bat :library:jvmRun

```

prints:
```console
Hello JVM World!
proper library name: model.dll
library search path: /path/to/library/src/jvmMain/lib/
Hello Jextract FFM World!
Dut.o=11

 - - - 

Hello Raw FFM World!
Dut.o=11

 - - - 

Hello my FFM World!
dut.o=11

 - - - 

Hello generated FFM World!
dut.o=11
```

### CIRCT Pipeline

![circt arcilator simulation pipeline](./arc/CIRCT-pipeline.excalidraw.svg)

#### Arcilator

State file (`--state-file`) contains observable model states—attributes of object such as registers, wires, memory, etc in program: 
- JSON format: https://github.com/llvm/circt/blob/d675c243c04339563517de1717dacbe3aa8309d5/lib/Dialect/Arc/ModelInfo.cpp#L130C3-L171C6
  - uses values, setted here: https://github.com/llvm/circt/blob/d675c243c04339563517de1717dacbe3aa8309d5/lib/Dialect/Arc/ModelInfo.cpp#L67C5-L100C6
    - uses such data struct: https://github.com/llvm/circt/blob/d675c243c04339563517de1717dacbe3aa8309d5/include/circt/Dialect/Arc/ModelInfo.h#L25C1-L40C38
    - and Ops generated with TableGen: https://github.com/llvm/circt/blob/main/include/circt/Dialect/Arc/ArcOps.td
    - and StateType from another generated header: https://github.com/llvm/circt/blob/main/include/circt/Dialect/Arc/ArcTypes.td
- in object file, function always names in `<module name>_eval` format:
  - https://github.com/llvm/circt/blob/d675c243c04339563517de1717dacbe3aa8309d5/lib/Conversion/ArcToLLVM/LowerArcToLLVM.cpp#L53C1-L55C2
  - C++ Header generator relies on it: https://github.com/llvm/circt/blob/d675c243c04339563517de1717dacbe3aa8309d5/tools/arcilator/arcilator-header-cpp.py#L12C1-L16C41
- `initialFnSym` and `finalFnSym` defined with tablegen:
  - https://github.com/llvm/circt/blob/2afb3cd0644297a6f7b7c185130bc4fcc0d3cd91/include/circt/Dialect/Arc/ArcOps.td#L483C1-L495C2

### TODO

- [ ] add constexpr model field "NUM_PORTS", "PORT_NAMES"
