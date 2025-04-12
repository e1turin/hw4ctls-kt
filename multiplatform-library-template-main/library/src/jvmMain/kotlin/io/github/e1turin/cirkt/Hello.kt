package io.github.e1turin.cirkt

import io.github.e1turin.cirkt.jextracted.State
import io.github.e1turin.cirkt.jextracted.dut_h
import io.github.e1turin.cirkt.sample.Dut
import io.github.e1turin.cirkt.sample.DutLibrary
import io.github.e1turin.cirkt.sample.dumpTo
import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemoryLayout
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout

val libName = "model"
val properLibName = System.mapLibraryName(libName) // 'model.dll' on Windows or 'libmodel.so' on linux

fun playWithFFM() {
    println("Hello JVM World!")

    System.loadLibrary(libName) // required for Windows

    println("proper library name: $properLibName")
    println("library search path: ${System.getProperty("java.library.path")}")

    jextractFFM() // does not load library automatically on Windows...

    println("\n - - - \n")

    rawFFM()

    println("\n - - - \n")

    myFfmWrapper()

    println("\n - - - \n")

//    generatedFfmWrapper()
}

//private fun generatedFfmWrapper() {
//    println("Hello generated FFM World!")
//    Arena.ofConfined().use { arena ->
//        val dut = io.github.e1turin.cirkt.generated.Dut.instance(arena, "model")
//
//        dut.reset = 1
//        for (i in 0..10) {
//            dut.clk = 1
//            dut.eval()
//            dut.clk = 0
//            dut.eval()
//        }
//
//        dut.reset = 0
//        for (i in 0..10) {
//            dut.clk = 1
//            dut.eval()
//            dut.clk = 0
//            dut.eval()
//        }
//
//        println("dut.o=${dut.o}")
//    }
//}

private fun myFfmWrapper() {
    println("Hello my FFM World!")
    Arena.ofConfined().use { arena ->
        val dut = Dut(arena, DutLibrary("model"))

        fun Dut.step(times: Int = 1) {
            for (i in 1..times) {
                clk = 1
                eval()
                clk = 0
                eval()
            }
        }

        fun Dut.reset(steps: Int = 0) {
            reset = 1
            eval()
            step(steps)
            reset = 0
        }

        dut.reset(10)
        dut.step(10)

        println("dut.o=${dut.o}")

        dut.dumpTo(object : StateDumper {
            override fun dumpStateByte(name: String, value: Byte) {
                println("$name: $value")
            }
        })
    }
}

private fun rawFFM() {
    println("Hello Raw FFM World!")

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

private fun jextractFFM() {
    println("Hello Jextract FFM World!")

    Arena.ofConfined().use {
        val state = it.allocate(State.layout())

        State.reset(state, 1)
        for (i in 0..10) {
            State.clk(state, 1)
            dut_h.Dut_eval(state)
            State.clk(state, 0)
            dut_h.Dut_eval(state)
        }

        State.reset(state, 0)
        for (i in 0..10) {
            State.clk(state, 1)
            dut_h.Dut_eval(state)
            State.clk(state, 0)
            dut_h.Dut_eval(state)
        }
        println("Dut.o=${state[ValueLayout.JAVA_BYTE, 7]}")
    }
}
