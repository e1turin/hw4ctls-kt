package io.github.e1turin.cirkt

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
    var clk by memoryAccess<Byte>(0, StateType.INPUT)

    //      {
    //        "name": "reset",
    //        "offset": 1,
    //        "numBits": 1,
    //        "type": "input"
    //      },
    var reset: Byte by memoryAccess<Byte>(1, StateType.INPUT)

    val internal = InternalStates()

    inner class InternalStates {
        //      {
        //        "name": "clk",
        //        "offset": 2,
        //        "numBits": 1,
        //        "type": "wire"
        //      },
        var clk: Byte by memoryAccess<Byte>(2, StateType.WIRE)

        //      {
        //        "name": "reset",
        //        "offset": 3,
        //        "numBits": 1,
        //        "type": "wire"
        //      },
        var reset: Byte by memoryAccess<Byte>(3, StateType.WIRE)

        //      {
        //        "name": "reg",
        //        "offset": 5,
        //        "numBits": 8,
        //        "type": "register"
        //      },
        var reg: Byte by memoryAccess<Byte>(5, StateType.REGISTER)

        //      {
        //        "name": "o",
        //        "offset": 6,
        //        "numBits": 8,
        //        "type": "wire"
        //      },
        var o: Byte by memoryAccess<Byte>(6, StateType.WIRE)
    }

    //      {
    //        "name": "o",
    //        "offset": 7,
    //        "numBits": 8,
    //        "type": "output"
    //      }
    var o: Byte by memoryAccess<Byte>(7, StateType.OUTPUT)

    fun eval() {
        lib.evalFunctionHandle.invokeExact(state)
    }

    fun initial() {
        lib.initialFunctionHandle.invokeExact(state)
    }

    fun final() {
        lib.finalFunctionHandle.invokeExact(state)
    }


    fun dump() = State(
        clk,
        reset,
        internal.clk,
        internal.reset,
        internal.reg,
        internal.o,
        o
    )

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

