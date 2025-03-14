module {
  llvm.mlir.global internal constant @"_arc_sim_emit_full_counter value"("counter value = %zx\0A\00") {addr_space = 0 : i32}
  llvm.func @printf(!llvm.ptr, ...)
  llvm.func @free(!llvm.ptr)
  llvm.func @malloc(i64) -> !llvm.ptr
  llvm.func @exit(i32) attributes {sym_visibility = "private"}
  llvm.func @counter_eval(%arg0: !llvm.ptr) {
    %0 = llvm.mlir.constant(1 : i8) : i8
    %1 = llvm.load %arg0 : !llvm.ptr -> i1
    %2 = llvm.getelementptr %arg0[1] : (!llvm.ptr) -> !llvm.ptr, i8
    llvm.store %1, %2 : i1, !llvm.ptr
    %3 = llvm.load %arg0 : !llvm.ptr -> i1
    %4 = llvm.getelementptr %arg0[2] : (!llvm.ptr) -> !llvm.ptr, i8
    %5 = llvm.load %4 : !llvm.ptr -> i1
    llvm.store %3, %4 : i1, !llvm.ptr
    %6 = llvm.xor %5, %3 : i1
    %7 = llvm.and %6, %3 : i1
    llvm.cond_br %7, ^bb1, ^bb2
  ^bb1:  // pred: ^bb0
    %8 = llvm.getelementptr %arg0[3] : (!llvm.ptr) -> !llvm.ptr, i8
    %9 = llvm.load %8 : !llvm.ptr -> i8
    %10 = llvm.add %9, %0 : i8
    llvm.store %10, %8 : i8, !llvm.ptr
    llvm.br ^bb2
  ^bb2:  // 2 preds: ^bb0, ^bb1
    %11 = llvm.getelementptr %arg0[3] : (!llvm.ptr) -> !llvm.ptr, i8
    %12 = llvm.load %11 : !llvm.ptr -> i8
    %13 = llvm.getelementptr %arg0[4] : (!llvm.ptr) -> !llvm.ptr, i8
    llvm.store %12, %13 : i8, !llvm.ptr
    %14 = llvm.load %11 : !llvm.ptr -> i8
    %15 = llvm.getelementptr %arg0[5] : (!llvm.ptr) -> !llvm.ptr, i8
    llvm.store %14, %15 : i8, !llvm.ptr
    llvm.return
  }
  llvm.func @entry() {
    %0 = llvm.mlir.addressof @"_arc_sim_emit_full_counter value" : !llvm.ptr
    %1 = llvm.mlir.constant(0 : i8) : i8
    %2 = llvm.mlir.constant(false) : i1
    %3 = llvm.mlir.constant(true) : i1
    %4 = llvm.mlir.constant(6 : i64) : i64
    %5 = llvm.call @malloc(%4) : (i64) -> !llvm.ptr
    "llvm.intr.memset"(%5, %1, %4) <{isVolatile = false}> : (!llvm.ptr, i8, i64) -> ()
    llvm.store %3, %5 : i1, !llvm.ptr
    llvm.call @counter_eval(%5) : (!llvm.ptr) -> ()
    llvm.store %2, %5 : i1, !llvm.ptr
    llvm.call @counter_eval(%5) : (!llvm.ptr) -> ()
    %6 = llvm.getelementptr %5[4] : (!llvm.ptr) -> !llvm.ptr, i8
    %7 = llvm.load %6 : !llvm.ptr -> i8
    %8 = llvm.zext %7 : i8 to i64
    llvm.call @printf(%0, %8) vararg(!llvm.func<void (ptr, ...)>) : (!llvm.ptr, i64) -> ()
    llvm.store %3, %5 : i1, !llvm.ptr
    llvm.call @counter_eval(%5) : (!llvm.ptr) -> ()
    llvm.store %2, %5 : i1, !llvm.ptr
    llvm.call @counter_eval(%5) : (!llvm.ptr) -> ()
    llvm.store %3, %5 : i1, !llvm.ptr
    llvm.call @counter_eval(%5) : (!llvm.ptr) -> ()
    llvm.store %2, %5 : i1, !llvm.ptr
    llvm.call @counter_eval(%5) : (!llvm.ptr) -> ()
    %9 = llvm.load %6 : !llvm.ptr -> i8
    %10 = llvm.zext %9 : i8 to i64
    llvm.call @printf(%0, %10) vararg(!llvm.func<void (ptr, ...)>) : (!llvm.ptr, i64) -> ()
    llvm.call @free(%5) : (!llvm.ptr) -> ()
    llvm.return
  }
}
