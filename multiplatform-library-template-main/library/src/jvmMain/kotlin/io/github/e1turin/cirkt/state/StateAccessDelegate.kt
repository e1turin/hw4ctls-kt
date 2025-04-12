package io.github.e1turin.cirkt.state

import io.github.e1turin.cirkt.Model
import io.github.e1turin.cirkt.Stateful
import java.lang.foreign.ValueLayout
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface StateAccessDelegate<T> : ReadWriteProperty<Stateful, T>
interface StateAccessDelegateProvider<T> : StateAccessDelegate<T>, PropertyDelegateProvider<Stateful, StateAccessDelegate<T>>

@PublishedApi
internal class ByteStateAccessDelegate(private val offset: Long) : StateAccessDelegate<Byte> {
    override fun getValue(thisRef: Stateful, property: KProperty<*>): Byte {
        return thisRef.state.get(ValueLayout.JAVA_BYTE, offset)
    }

    override fun setValue(thisRef: Stateful, property: KProperty<*>, value: Byte) {
        thisRef.state.set(ValueLayout.JAVA_BYTE, offset, value)
    }
}

@PublishedApi
internal class IntStateAccessDelegate(private val offset: Long) : StateAccessDelegate<Int> {
    override fun getValue(thisRef: Stateful, property: KProperty<*>): Int {
        return thisRef.state.get(ValueLayout.JAVA_INT, offset)
    }

    override fun setValue(thisRef: Stateful, property: KProperty<*>, value: Int) {
        thisRef.state.set(ValueLayout.JAVA_INT, offset, value)
    }
}

@PublishedApi
internal class LongStateAccessDelegate(private val offset: Long) : StateAccessDelegate<Long> {
    override fun getValue(thisRef: Stateful, property: KProperty<*>): Long {
        return thisRef.state.get(ValueLayout.JAVA_LONG, offset)
    }

    override fun setValue(thisRef: Stateful, property: KProperty<*>, value: Long) {
        thisRef.state.set(ValueLayout.JAVA_LONG, offset, value)
    }
}

// TODO: Other type delegates

fun scaledOffset(offset: Long, byteSize: Long): Long {
    require(offset % byteSize == 0L) {
        "Accessed bytes should be aligned according to underlying data type size"
    }
    return offset / byteSize
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T> Model.stateAccess(offset: Long, stateType: StateType): StateAccessDelegate<T> {
    require(offset < numStateBytes) { "Offset $offset bounded to state size $numStateBytes" }

    val clazz = T::class

    val byteSize: Long
    val property = when (clazz) {
        Byte::class -> {
            byteSize = ValueLayout.JAVA_BYTE.byteSize()
            ByteStateAccessDelegate(scaledOffset(offset, byteSize))
        }

        Int::class -> {
            byteSize = ValueLayout.JAVA_INT.byteSize()
            IntStateAccessDelegate(scaledOffset(offset, byteSize))
        }

        Long::class -> {
            byteSize = ValueLayout.JAVA_LONG.byteSize()
            LongStateAccessDelegate(scaledOffset(offset, byteSize))
        }

        else -> throw NotImplementedError("Unsupported Type for memory access: ${T::class.qualifiedName}")
    } as StateAccessDelegate<T>

    require(offset + byteSize in 1..numStateBytes) {
        "Accessed memory - $byteSize bytes for ${clazz.qualifiedName} (${stateType.name}) on offset $offset -" +
            " must be in bounds of state size $numStateBytes but it is not!"
    }

    return property
}

inline fun <reified T> Model.input(offset: Long) = stateAccess<T>(offset, StateType.INPUT)
inline fun <reified T> Model.output(offset: Long) = stateAccess<T>(offset, StateType.OUTPUT)
inline fun <reified T> Model.register(offset: Long) = stateAccess<T>(offset, StateType.REGISTER)
inline fun <reified T> Model.memory(offset: Long) = stateAccess<T>(offset, StateType.MEMORY)
inline fun <reified T> Model.wire(offset: Long) = stateAccess<T>(offset, StateType.WIRE)

