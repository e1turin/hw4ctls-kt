package io.github.e1turin.cirkt.state

import io.github.e1turin.cirkt.model.Model
import java.lang.foreign.ValueLayout
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface StateProjection<T> {
    fun getProjectionValueOf(stateful: Stateful): T
    fun setProjectionOf(stateful: Stateful, value: T)
}

interface StateProjectionDelegate<T> : StateProjection<T>, ReadWriteProperty<Stateful, T> {
    override fun getValue(thisRef: Stateful, property: KProperty<*>): T {
        return getProjectionValueOf(thisRef)
    }

    override fun setValue(thisRef: Stateful, property: KProperty<*>, value: T) {
        setProjectionOf(thisRef, value)
    }
}

interface StateProjectionDelegateProvider<T> : StateProjectionDelegate<T>,
    PropertyDelegateProvider<Stateful, StateProjectionDelegate<T>>

@PublishedApi
internal class ByteStateProjection(private val offset: Long) : StateProjectionDelegate<Byte> {
    override fun getProjectionValueOf(stateful: Stateful): Byte {
        return stateful.state.get(ValueLayout.JAVA_BYTE, offset)
    }

    override fun setProjectionOf(stateful: Stateful, value: Byte) {
        stateful.state.set(ValueLayout.JAVA_BYTE, offset, value)
    }
}

@PublishedApi
internal class IntStateProjection(private val offset: Long) : StateProjectionDelegate<Int> {
    override fun getProjectionValueOf(stateful: Stateful): Int {
        return stateful.state.get(ValueLayout.JAVA_INT, offset)
    }

    override fun setProjectionOf(stateful: Stateful, value: Int) {
        stateful.state.set(ValueLayout.JAVA_INT, offset, value)
    }
}

@PublishedApi
internal class LongStateProjection(private val offset: Long) : StateProjectionDelegate<Long> {
    override fun getProjectionValueOf(stateful: Stateful): Long {
        return stateful.state.get(ValueLayout.JAVA_LONG, offset)
    }

    override fun setProjectionOf(stateful: Stateful, value: Long) {
        stateful.state.set(ValueLayout.JAVA_LONG, offset, value)
    }
}

// TODO: Other type delegates

@PublishedApi
internal fun scaledOffset(offset: Long, byteSize: Long): Long {
    require(offset % byteSize == 0L) {
        "Accessed bytes should be aligned according to underlying data type size"
    }
    return offset / byteSize
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T> Model.stateProjection(
    offset: Long,
    projectionType: StateProjectionType
): StateProjectionDelegate<T> {
    require(offset < numStateBytes) { "Offset $offset bounded to state size $numStateBytes" }

    val clazz = T::class

    val byteSize: Long
    val property = when (clazz) {
        Byte::class -> {
            byteSize = ValueLayout.JAVA_BYTE.byteSize()
            ByteStateProjection(scaledOffset(offset, byteSize))
        }

        Int::class -> {
            byteSize = ValueLayout.JAVA_INT.byteSize()
            IntStateProjection(scaledOffset(offset, byteSize))
        }

        Long::class -> {
            byteSize = ValueLayout.JAVA_LONG.byteSize()
            LongStateProjection(scaledOffset(offset, byteSize))
        }

        else -> throw NotImplementedError("Unsupported Type for memory access: ${T::class.qualifiedName}")
    } as StateProjectionDelegate<T>

    require(offset + byteSize in 1..numStateBytes) {
        "Accessed memory - $byteSize bytes for ${clazz.qualifiedName} (${projectionType.name}) on offset $offset -" +
            " must be in bounds of state size $numStateBytes but it is not!"
    }

    return property
}

inline fun <reified T> Model.input(offset: Long) = stateProjection<T>(offset, StateProjectionType.INPUT)
inline fun <reified T> Model.output(offset: Long) = stateProjection<T>(offset, StateProjectionType.OUTPUT)
inline fun <reified T> Model.register(offset: Long) = stateProjection<T>(offset, StateProjectionType.REGISTER)
inline fun <reified T> Model.memory(offset: Long) = stateProjection<T>(offset, StateProjectionType.MEMORY)
inline fun <reified T> Model.wire(offset: Long) = stateProjection<T>(offset, StateProjectionType.WIRE)

