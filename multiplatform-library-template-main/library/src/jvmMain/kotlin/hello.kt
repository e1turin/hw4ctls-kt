package io.github.kotlin.fibonacci

import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemoryLayout
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout

fun main(): Unit {
    println("Hello JVM World!")

    System.loadLibrary("model")
    val lookup = SymbolLookup.libraryLookup("model", Arena.ofAuto())//.loaderLookup()

    val linker = Linker.nativeLinker()

    val dutEval = linker.downcallHandle(
        lookup.find("Dut_eval").get(),
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    )

    val stateLayout = MemoryLayout.sequenceLayout(8, ValueLayout.JAVA_BYTE)
    Arena.ofConfined().use {
        val state = it.allocate(8)

        // reset <= 1
        state[ValueLayout.JAVA_BYTE, 1] = 1
        for (i in 0..10) {
            // clk <= 1
            state[ValueLayout.JAVA_BYTE, 0] = 1
            dutEval.invokeExact(state)
            // clk <= 0
            state[ValueLayout.JAVA_BYTE, 0] = 0
            dutEval.invokeExact(state)
        }

        // reset <= 0
        state[ValueLayout.JAVA_BYTE, 1] = 0
        for (i in 0..10) {
            // clk <= 1
            state[ValueLayout.JAVA_BYTE, 0] = 1
            dutEval.invokeExact(state)
            // clk <= 0
            state[ValueLayout.JAVA_BYTE, 0] = 0
            dutEval.invokeExact(state)
        }
        println("Dut.o=${state[ValueLayout.JAVA_BYTE, 7]}")
    }

}
