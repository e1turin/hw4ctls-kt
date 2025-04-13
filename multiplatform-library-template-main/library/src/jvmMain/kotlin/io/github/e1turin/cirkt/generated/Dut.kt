package io.github.e1turin.cirkt.generated

import io.github.e1turin.cirkt.model.Model
import io.github.e1turin.cirkt.model.ModelLibrary
import io.github.e1turin.cirkt.state.input
import io.github.e1turin.cirkt.state.output
import io.github.e1turin.cirkt.state.register
import io.github.e1turin.cirkt.state.wire
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.invoke.MethodHandle
import kotlin.Byte
import kotlin.Long
import kotlin.String

public open class DutModel(
  state: MemorySegment,
  lib: ModelLibrary,
) : Model(MODEL_NAME, state, lib, NUM_STATE_BYTES) {
  public open var clk: Byte by input<Byte>(0)

  public open var reset: Byte by input<Byte>(1)

  public open var internalClk: Byte by wire<Byte>(2)

  public open var internalReset: Byte by wire<Byte>(3)

  public open var internalReg: Byte by register<Byte>(5)

  public open var internalO: Byte by wire<Byte>(6)

  public open var o: Byte by output<Byte>(7)

  public fun eval() {
    lib.evalFunctionHandle.invokeExact(state)
  }

  public fun initial() {
    lib.initialFunctionHandle.invokeExact(state)
  }

  public fun `final`() {
    lib.finalFunctionHandle.invokeExact(state)
  }

  public companion object {
    public const val MODEL_NAME: String = "Dut"

    public const val NUM_STATE_BYTES: Long = 8L

    public fun instance(
      arena: Arena,
      libraryName: String,
      libraryArena: Arena = Arena.ofAuto(),
    ): DutModel = DutModel(arena.allocate(NUM_STATE_BYTES), DutLibrary(libraryName, libraryArena))
  }
}

public open class DutLibrary(
  name: String,
  arena: Arena = Arena.ofAuto(),
) : ModelLibrary(name, arena, "Dut_eval", "", "") {
  override val evalFunctionHandle: MethodHandle = functionHandle(evalFnSym)

  override val initialFunctionHandle: MethodHandle
    get() = throw NotImplementedError("Symbol initialFnSym is not defined")

  override val finalFunctionHandle: MethodHandle
    get() = throw NotImplementedError("Symbol finalFnSym is not defined")
}
