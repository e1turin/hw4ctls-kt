package io.github.e1turin.cirkt.state

import java.lang.foreign.MemorySegment

interface Stateful {
    val state: MemorySegment
}
