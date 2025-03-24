package io.github.kotlin.fibonacci

import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class)
actual fun main() {
    println("Hello Native world!")
    memScoped {
        var state = alloc<dut.State>()

        state.clk = 0
        state.reset = 1
        for (i in 0..10) {
            state.clk = 1
            dut.Dut_eval(state.ptr)
            state.clk = 0
            dut.Dut_eval(state.ptr)
        }
        state.reset = 0
        for (i in 0..10) {
            state.clk = 1
            dut.Dut_eval(state.ptr)
            state.clk = 0
            dut.Dut_eval(state.ptr)
        }
        println(state.o)
    }
}
