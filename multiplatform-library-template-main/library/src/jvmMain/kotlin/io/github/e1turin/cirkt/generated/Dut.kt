package io.github.e1turin.cirkt.generated

import io.github.e1turin.cirkt.Model
import io.github.e1turin.cirkt.ModelLibrary
import io.github.e1turin.cirkt.memoryAccess
import io.github.e1turin.cirkt.state.StateType
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.invoke.MethodHandle
import kotlin.Byte
import kotlin.Long
import kotlin.String

public open class Dut(
  state: MemorySegment,
  lib: ModelLibrary,
) : Model("Dut", state, lib, 8) {
  public var clk: Byte by memoryAccess<Byte>(0, StateType.INPUT)

  public var reset: Byte by memoryAccess<Byte>(1, StateType.INPUT)

  public val o: Byte by memoryAccess<Byte>(7, StateType.OUTPUT)

  public val `internal`: InternalStates = InternalStates()

  public fun eval() {
    lib.evalFunctionHandle.invokeExact(state)
  }

  public fun initial() {
    lib.initialFunctionHandle.invokeExact(state)
  }

  public fun `final`() {
    lib.finalFunctionHandle.invokeExact(state)
  }

  public fun dump(): State = State(clk, reset, internal.clk, internal.reset, internal.reg, internal.o, o)

  public inner class InternalStates {
    public var clk: Byte by memoryAccess<Byte>(2, StateType.WIRE)

    public var reset: Byte by memoryAccess<Byte>(3, StateType.WIRE)

    public var reg: Byte by memoryAccess<Byte>(5, StateType.REGISTER)

    public var o: Byte by memoryAccess<Byte>(6, StateType.WIRE)
  }

  public data class State(
    public val clk: Byte,
    public val reset: Byte,
    public val internalClk: Byte,
    public val internalReset: Byte,
    public val internalReg: Byte,
    public val internalO: Byte,
    public val o: Byte,
  )

  public companion object {
    public const val NUM_STATE_BYTES: Long = 8L

    public fun library(name: String, arena: Arena): ModelLibrary = object : ModelLibrary(name, arena, "Dut_eval", "", "") {
      override val evalFunctionHandle: MethodHandle = functionHandle(evalFnSym)
      override val initialFunctionHandle: MethodHandle by lazy {
        stubFunctionHandle("initialFnSym is blank: '${'$'}initialFnSym'")
      }
      override val finalFunctionHandle: MethodHandle by lazy {
        stubFunctionHandle("finalFnSym is blank: '${'$'}finalFnSym'")
      }
    }

    public fun instance(
      stateArena: Arena = Arena.ofAuto(),
      libName: String,
      libArena: Arena = Arena.ofAuto(),
    ): Dut = Dut(stateArena.allocate(NUM_STATE_BYTES), library(libName, libArena))
  }
}
