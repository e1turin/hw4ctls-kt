package io.github.e1turin.cirkt.model

import io.github.e1turin.cirkt.state.Stateful
import java.lang.foreign.MemorySegment

abstract class Model(
    val name: String,
    override val state: MemorySegment,
    val lib: ModelLibrary,
    val numStateBytes: Long
) : Stateful

