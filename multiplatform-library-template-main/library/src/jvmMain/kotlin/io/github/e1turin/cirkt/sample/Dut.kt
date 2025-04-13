package io.github.e1turin.cirkt.sample

import io.github.e1turin.cirkt.model.Model
import io.github.e1turin.cirkt.model.ModelLibrary
import io.github.e1turin.cirkt.StateDumper
import io.github.e1turin.cirkt.state.input
import io.github.e1turin.cirkt.state.output
import io.github.e1turin.cirkt.state.register
import io.github.e1turin.cirkt.state.wire
import java.lang.foreign.*
import java.lang.invoke.MethodHandle

/*
 open class gives ability to extend model e.g. create composite device
 where other states taken from another model
 */
open class Dut(
    state: MemorySegment,
    lib: ModelLibrary
) : Model(MODEL_NAME, state, lib, NUM_STATE_BYTES) {

    /*
     * {
     *   "name": "clk",
     *   "offset": 0,
     *   "numBits": 1,
     *   "type": "input"
     * },
     */
    open var clk by input<Byte>(0)

    /*
     * {
     *   "name": "reset",
     *   "offset": 1,
     *   "numBits": 1,
     *   "type": "input"
     * },
     */
    open var reset: Byte by input<Byte>(1)

    /*
     * {
     *   "name": "clk",
     *   "offset": 2,
     *   "numBits": 1,
     *   "type": "wire"
     * },
     */
    open var internalClk: Byte by wire<Byte>(2)

    /*
             * {
             *   "name": "reset",
             *   "offset": 3,
             *   "numBits": 1,
             *   "type": "wire"
             * },
             */
    open var internalReset: Byte by wire<Byte>(3)

    /*
             * {
             *   "name": "reg",
             *   "offset": 5,
             *   "numBits": 8,
             *   "type": "register"
             * },
             */
    open var internalReg: Byte by register<Byte>(5)

    /*
             * {
             *   "name": "o",
             *   "offset": 6,
             *   "numBits": 8,
             *   "type": "wire"
             * },
             */
    open var internalO: Byte by wire<Byte>(6)

    /*
     * {
     *   "name": "o",
     *   "offset": 7,
     *   "numBits": 8,
     *   "type": "output"
     * }
     */
    open var o: Byte by output<Byte>(7)

    open fun eval() {
        lib.evalFunctionHandle.invokeExact(state)
    }

    open fun initial() {
        lib.initialFunctionHandle.invokeExact(state)
    }

    open fun final() {
        lib.finalFunctionHandle.invokeExact(state)
    }

    companion object {
        /* "name": "Dut", */
        const val MODEL_NAME: String = "Dut"

        /* "numStateBytes": 8, */
        const val NUM_STATE_BYTES: Long = 8

        fun instance(arena: Arena, libraryName: String, libraryArena: Arena = Arena.ofAuto()): Dut = Dut(
            arena.allocate(NUM_STATE_BYTES),
            DutLibrary(libraryName, libraryArena)
        )
    }
}

class DutLibrary(name: String, arena: Arena = Arena.ofAuto()) : ModelLibrary(
    name,
    arena,
    /* "name": "Dut", */
    evalFnSym = "Dut_eval",
    /* "initialFnSym": "", */
    initialFnSym = "",
    /* "finalFnSym": "", */
    finalFnSym = ""
) {
    override val evalFunctionHandle: MethodHandle
        get() = functionHandle(evalFnSym)
    override val initialFunctionHandle: MethodHandle
        get() = throw NotImplementedError("No such symbol")
    override val finalFunctionHandle: MethodHandle
        get() = TODO("Not yet implemented")
}

// move to object?
fun Dut.dumpTo(dumper: StateDumper): Unit = with(dumper) {
    dumpStateByte("$name.clk", clk)
    dumpStateByte("$name.reset", reset)
    dumpStateByte("$name.o", o)
    dumpStateByte("$name.internal.clk", internalClk)
    dumpStateByte("$name.internal.reset", internalReset)
    dumpStateByte("$name.internal.reg", internalReg)
    dumpStateByte("$name.internal.o", internalO)
}

