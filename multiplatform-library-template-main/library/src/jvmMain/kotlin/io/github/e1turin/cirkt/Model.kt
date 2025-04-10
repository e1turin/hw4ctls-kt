package io.github.e1turin.cirkt

import io.github.e1turin.cirkt.state.DumpStateVisitor
import io.github.e1turin.cirkt.state.Dumpable
import java.lang.foreign.MemorySegment

abstract class Model(
    val name: String,
    val state: MemorySegment,
    val lib: ModelLibrary,
    val numStateBytes: Long
) : Stateful(name)

abstract class Stateful(
    val stateGroupName: String
) {
    private val stateProjections: MutableList<Dumpable> = mutableListOf()

    fun addStateProjection(stateProjection: Dumpable): Boolean = stateProjections.add(stateProjection)

    open fun dumpStateTo(visitor: DumpStateVisitor) = stateProjections.forEach { it.dumpTo(visitor) }
}
