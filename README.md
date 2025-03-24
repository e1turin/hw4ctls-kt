# hw4ctls-kt

Experiments with CIRCT: use infrastracture for modeling hardware in Kotlin.

Some source files are taken from  https://github.com/circt/circt and https://github.com/circt/arc-tests repositories.

Build script inside `arc/`:
```sh
arcilator model.mlir --emit-llvm --observe-memories --observe-named-values --observe-ports --observe-registers --observe-wires --state-file=out/model-states.json -o out/model.llvm
llc out/model.llvm -O3 --filetype=obj -o out/model.o
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

```sh
llvm-ar rc out/libdut.a out/model.o
# copu libdut.a to src/mingwX64Main/kotlin/c/
gradlew.bat :mingwX64Binaries
# cd library\build\bin\mingwX64\releaseExecutable
./library.exe
```

prints:
```console
Hello Native world!
11
```

![circt arcilator simulation pipeline](./arc/CIRCT-pipeline.excalidraw.svg)


TODO:
- add reset to Dut
- add constexpr model field "NUM_PORTS", "PORT_NAMES"
