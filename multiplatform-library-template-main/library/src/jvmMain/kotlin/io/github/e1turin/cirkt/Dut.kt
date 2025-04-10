package io.github.e1turin.cirkt

import io.github.e1turin.cirkt.state.*
import java.lang.foreign.*
import java.lang.invoke.MethodHandle

class Dut(
    state: MemorySegment,
    lib: ModelLibrary
    //    "name": "Dut",
) : Model("Dut", state, lib, NUM_STATE_BYTES) {

    data class State(
        val clk: Byte,
        val reset: Byte,
        val internalClk: Byte,
        val internalReset: Byte,
        val internalReg: Byte,
        val internalO: Byte,
        val o: Byte
    )

    //      {
    //        "name": "clk",
    //        "offset": 0,
    //        "numBits": 1,
    //        "type": "input"
    //      },
    var clk by input<Byte>(0)

    //      {
    //        "name": "reset",
    //        "offset": 1,
    //        "numBits": 1,
    //        "type": "input"
    //      },
    var reset: Byte by input<Byte>(1)

    val internal = InternalStates()

    inner class InternalStates : Stateful("$stateGroupName.internal") {
        //      {
        //        "name": "clk",
        //        "offset": 2,
        //        "numBits": 1,
        //        "type": "wire"
        //      },
        var clk: Byte by wire<Byte>(2)

        //      {
        //        "name": "reset",
        //        "offset": 3,
        //        "numBits": 1,
        //        "type": "wire"
        //      },
        var reset: Byte by wire<Byte>(3)

        //      {
        //        "name": "reg",
        //        "offset": 5,
        //        "numBits": 8,
        //        "type": "register"
        //      },
        var reg: Byte by register<Byte>(5)

        //      {
        //        "name": "o",
        //        "offset": 6,
        //        "numBits": 8,
        //        "type": "wire"
        //      },
        var o: Byte by wire<Byte>(6)
    }

    //      {
    //        "name": "o",
    //        "offset": 7,
    //        "numBits": 8,
    //        "type": "output"
    //      }
    var o: Byte by output<Byte>(7)

    fun eval() {
        lib.evalFunctionHandle.invokeExact(state)
    }

    fun initial() {
        lib.initialFunctionHandle.invokeExact(state)
    }

    fun final() {
        lib.finalFunctionHandle.invokeExact(state)
    }

    override fun dumpStateTo(visitor: DumpStateVisitor) {
        super.dumpStateTo(visitor)
        internal.dumpStateTo(visitor)
    }

    companion object {
        //    "numStateBytes": 8,
        const val NUM_STATE_BYTES: Long = 8

        fun library(name: String, arena: Arena) = object : ModelLibrary(
            name,
            arena,
            //    "name": "Dut",
            evalFnSym = "Dut_eval",
            //    "initialFnSym": "",
            initialFnSym = "",
            //    "finalFnSym": "",
            finalFnSym = ""
        ) {
            override val evalFunctionHandle: MethodHandle = functionHandle(evalFnSym)
            override val initialFunctionHandle: MethodHandle by lazy { stubFunctionHandle("initialFnSym is blank: '$initialFnSym'") }
            override val finalFunctionHandle: MethodHandle by lazy { stubFunctionHandle("finalFnSym is blank: '$finalFnSym'") }
        }

        fun instance(stateArena: Arena, libName: String, libArena: Arena = Arena.ofAuto()) =
            Dut(stateArena.allocate((NUM_STATE_BYTES)), library(libName, libArena))
    }

}

