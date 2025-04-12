package io.github.e1turin.cirkt

import java.lang.foreign.MemorySegment

interface Stateful {
    val state: MemorySegment
}

abstract class Model(
    val name: String,
    override val state: MemorySegment,
    val lib: ModelLibrary,
    val numStateBytes: Long
) : Stateful

