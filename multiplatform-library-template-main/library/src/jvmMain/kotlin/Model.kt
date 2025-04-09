package io.github.e1turin.cirkt

import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class Model(val name: String, val state: MemorySegment, val lib: ModelLibrary, val numStateBytes: Long)

interface MemoryAccessProperty<T> : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T)

    val accessedBytes: Long

    fun scaledOffset(offset: Long): Long {
        require(offset % accessedBytes == 0L) {
            "Accessed bytes should be aligned according to underlying data type size"
        }
        return offset / accessedBytes
    }
}

@PublishedApi
internal class MemoryAccessByte(val state: MemorySegment, offset: Long) : MemoryAccessProperty<Byte> {
    override val accessedBytes: Long = ValueLayout.JAVA_BYTE.byteSize()
    private val offsetByByte: Long = scaledOffset(offset)

    override fun getValue(thisRef: Any?, property: KProperty<*>): Byte {
        return state.get(ValueLayout.JAVA_BYTE, offsetByByte)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Byte) {
        state.set(ValueLayout.JAVA_BYTE, offsetByByte, value)
    }
}

@PublishedApi
internal class MemoryAccessInt(val state: MemorySegment, offset: Long) : MemoryAccessProperty<Int> {
    override val accessedBytes: Long = ValueLayout.JAVA_INT.byteSize()
    private val offsetByInt: Long = scaledOffset(offset)

    override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return state.get(ValueLayout.JAVA_INT, offsetByInt)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        state.set(ValueLayout.JAVA_INT, offsetByInt, value)
    }
}

@PublishedApi
internal class MemoryAccessLong(val state: MemorySegment, offset: Long) : MemoryAccessProperty<Long> {
    override val accessedBytes: Long = ValueLayout.JAVA_LONG.byteSize()
    private val offsetByLong: Long = scaledOffset(offset)

    override fun getValue(thisRef: Any?, property: KProperty<*>): Long {
        return state.get(ValueLayout.JAVA_LONG, offsetByLong)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
        state.set(ValueLayout.JAVA_LONG, offsetByLong, value)
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T> Model.memoryAccess(offset: Long, type: StateType): MemoryAccessProperty<T> {
    require(offset < numStateBytes) { "Offset $offset bounded to state size $numStateBytes" }

    val clazz = T::class

    val property = when (clazz) {
        Byte::class -> MemoryAccessByte(state, offset)

        Int::class -> MemoryAccessInt(state, offset)

        Long::class -> MemoryAccessLong(state, offset)

        else -> throw NotImplementedError("Unsupported Type for memory access: ${T::class.qualifiedName}")
    } as MemoryAccessProperty<T>

    require(offset + property.accessedBytes in 1..numStateBytes) {
        "Accessed memory ($property.accessedBytes bytes for ${clazz.qualifiedName} from offset $offset) must be in bounds of state size $numStateBytes"
    }

    return property
}
