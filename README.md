# hw4ctls-kt

Experiments with CIRCT: use infrastracture for modeling hardware in Kotlin.

Some source files are taken from  https://github.com/circt/circt and https://github.com/circt/arc-tests repositories.

```sh
cd arc

arcilator model.mlir --run --print-debug-info --observe-wires --observe-registers --observe-ports
## Outputs:
#> dut o = 1

arcilator model.mlir --emit-mlir --print-debug-info --observe-wires --observe-registers --observe-ports | save -f model-out.mlir

arcilator model.mlir --emit-llvm --print-debug-info --observe-wires --observe-registers --observe-ports | save -f model.llvm

arcilator model.mlir --state-file=model.json --print-debug-info --observe-wires --observe-registers --observe-ports | llc -O3 --filetype=obj -o model.o
llvm-objdump --disassemble model.o | save -f model.S
# similar to
arcilator model.mlir --state-file=model.json --print-debug-info --observe-wires --observe-registers --observe-ports | llc -O3 --filetype=asm -o model.asm


python arcilator-header-cpp.py model.json --view-depth 1 | save -f model.h

clang++ model-main.cpp model.o -o model-main.exe
./model-main.exe
## Outputs:
#> dut o = 20
#> total time = 1700
 ```

 > total time is approximate and varies from 1400 to 2400 for 20 clock cycles

![circt arcilator simulation pipeline](./arc/CIRCT-pipeline.excalidraw.svg)


TODO:
- add reset to Dut
- add constexpr model field "NUM_PORTS", "PORT_NAMES"
