package io.github.e1turin.cirkt.state

import io.github.e1turin.cirkt.Model
import io.github.e1turin.cirkt.Stateful
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface StateAccessDelegate<T> : ReadWriteProperty<Stateful, T>

interface StateAccessDelegateProvider<T> : StateAccessDelegate<T>, Dumpable {
    operator fun provideDelegate(thisRef: Stateful, property: KProperty<*>): StateAccessDelegateProvider<T>
}

internal abstract class AbstractStateAccessDelegate<T, L : ValueLayout>(
    protected val state: MemorySegment,
    offset: Long,
    valueLayout: L,
) : StateAccessDelegateProvider<T> {
    protected val offset: Long = scaledOffset(offset, valueLayout.byteSize())
    protected var name: String = ""

    override operator fun provideDelegate(thisRef: Stateful, property: KProperty<*>): StateAccessDelegateProvider<T> {
        name = "${thisRef.stateGroupName}.${property.name}"
        thisRef.addStateProjection(this)
        return this
    }

    fun scaledOffset(offset: Long, byteSize: Long): Long {
        require(offset % byteSize == 0L) {
            "Accessed bytes should be aligned according to underlying data type size"
        }
        return offset / byteSize
    }
}

@PublishedApi
internal class ByteStateAccessDelegate(state: MemorySegment, offset: Long) :
    AbstractStateAccessDelegate<Byte, ValueLayout.OfByte>(state, offset, ValueLayout.JAVA_BYTE) {

    override fun getValue(thisRef: Stateful, property: KProperty<*>): Byte {
        return state.get(ValueLayout.JAVA_BYTE, offset)
    }

    override fun setValue(thisRef: Stateful, property: KProperty<*>, value: Byte) {
        state.set(ValueLayout.JAVA_BYTE, offset, value)
    }

    override fun dumpTo(visitor: DumpStateVisitor) {
        visitor.dumpStateByte(name, state.get(ValueLayout.JAVA_BYTE, offset))
    }
}

@PublishedApi
internal class IntStateAccessDelegate(state: MemorySegment, offset: Long) :
    AbstractStateAccessDelegate<Int, ValueLayout.OfInt>(state, offset, ValueLayout.JAVA_INT) {

    override fun getValue(thisRef: Stateful, property: KProperty<*>): Int {
        return state.get(ValueLayout.JAVA_INT, offset)
    }

    override fun setValue(thisRef: Stateful, property: KProperty<*>, value: Int) {
        state.set(ValueLayout.JAVA_INT, offset, value)
    }

    override fun dumpTo(visitor: DumpStateVisitor) {
        visitor.dumpStateInt(name, state.get(ValueLayout.JAVA_INT, offset))
    }
}

@PublishedApi
internal class LongStateAccessDelegate(state: MemorySegment, offset: Long) :
    AbstractStateAccessDelegate<Long, ValueLayout.OfLong>(state, offset, ValueLayout.JAVA_LONG) {

    override fun getValue(thisRef: Stateful, property: KProperty<*>): Long {
        return state.get(ValueLayout.JAVA_LONG, offset)
    }

    override fun setValue(thisRef: Stateful, property: KProperty<*>, value: Long) {
        state.set(ValueLayout.JAVA_LONG, offset, value)
    }

    override fun dumpTo(visitor: DumpStateVisitor) {
        visitor.dumpStateLong(name, state.get(ValueLayout.JAVA_LONG, offset))
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T> Model.stateAccess(offset: Long, stateType: StateType): StateAccessDelegateProvider<T> {
    require(offset < numStateBytes) { "Offset $offset bounded to state size $numStateBytes" }

    val clazz = T::class

    val byteSize: Long
    val property = when (clazz) {
        Byte::class -> {
            byteSize = ValueLayout.JAVA_BYTE.byteSize()
            ByteStateAccessDelegate(state, offset)
        }

        Int::class -> {
            byteSize = ValueLayout.JAVA_INT.byteSize()
            IntStateAccessDelegate(state, offset)
        }

        Long::class -> {
            byteSize = ValueLayout.JAVA_LONG.byteSize()
            LongStateAccessDelegate(state, offset)
        }

        else -> throw NotImplementedError("Unsupported Type for memory access: ${T::class.qualifiedName}")
    } as StateAccessDelegateProvider<T>

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
