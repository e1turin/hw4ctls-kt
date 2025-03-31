package io.github.e1turin.cirkt

import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemoryLayout
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout

fun main(): Unit {
    println("Hello JVM World!")

    val libName = "model"
    System.loadLibrary(libName) // required for Windows

    val properLibName = System.mapLibraryName(libName) // 'model.dll' on Windows or 'libmodel.so' on linux
    println("proper library name: $properLibName")
    println("library search path: ${System.getProperty("java.library.path")}")

    val lookup = SymbolLookup.libraryLookup(properLibName, Arena.ofAuto())
        .or(SymbolLookup.loaderLookup())
        .or(Linker.nativeLinker().defaultLookup())

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
