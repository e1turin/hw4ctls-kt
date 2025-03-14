# hw4ctls-kt

Experiments with CIRCT: use infrastracture for modeling hardware in Kotlin.

Some source files are taken from  https://github.com/circt/circt and https://github.com/circt/arc-tests repositories.

```sh
cd arc

arcilator --run --print-debug-info --observe-wires --observe-registers --observe-ports  model.mlir
# Outputs:
# counter value = 1
# counter value = 3

arcilator --emit-mlir --print-debug-info --observe-wires --observe-registers --observe-ports  model.mlir | save -f model-out.mlir

arcilator --emit-llvm --print-debug-info --observe-wires --observe-registers --observe-ports  model.mlir | save -f model.llvm

arcilator model.mlir --state-file=model.json --print-debug-info --observe-wires --observe-registers --observe-ports | llc -O3 --filetype=obj -o model.o
llvm-objdump --disassemble model.o | save -f model.S
# similar to
arcilator model.mlir --state-file=model.json --print-debug-info --observe-wires --observe-registers --observe-ports | llc -O3 --filetype=asm -o model.asm


python arcilator-header-cpp.py model.json --view-depth 1 | save -f model.h
 ```

![circt arcilator simulation pipeline](./arc/CIRCT-pipeline.excalidraw.svg)
