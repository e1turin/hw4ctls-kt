package io.github.e1turin.cirkt

import java.lang.foreign.*
import java.lang.invoke.MethodHandle
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Dut(
    state: MemorySegment,
    lib: ModuleLibrary
    //    "name": "Dut",
) : Module("Dut", state, lib, NUM_STATE_BYTES) {

    data class State(
        val clk: Byte,
        val reset: Byte,
        val internalClk: Byte,
        val internalReset: Byte,
        val internalReg: Byte,
        val internalO: Byte,
        val o: Byte
    )

    //      {
    //        "name": "clk",
    //        "offset": 0,
    //        "numBits": 1,
    //        "type": "input"
    //      },
    var clk by memoryAccess<Byte>(0)

    //      {
    //        "name": "reset",
    //        "offset": 1,
    //        "numBits": 1,
    //        "type": "input"
    //      },
    var reset: Byte by memoryAccess<Byte>(1)

    val internal = InternalStates()

    inner class InternalStates {
        //      {
        //        "name": "clk",
        //        "offset": 2,
        //        "numBits": 1,
        //        "type": "wire"
        //      },
        var clk: Byte by memoryAccess<Byte>(2)

        //      {
        //        "name": "reset",
        //        "offset": 3,
        //        "numBits": 1,
        //        "type": "wire"
        //      },
        var reset: Byte by memoryAccess<Byte>(3)

        //      {
        //        "name": "reg",
        //        "offset": 5,
        //        "numBits": 8,
        //        "type": "register"
        //      },
        var reg: Byte by memoryAccess<Byte>(5)

        //      {
        //        "name": "o",
        //        "offset": 6,
        //        "numBits": 8,
        //        "type": "wire"
        //      },
        var o: Byte by memoryAccess<Byte>(6)
    }

    //      {
    //        "name": "o",
    //        "offset": 7,
    //        "numBits": 8,
    //        "type": "output"
    //      }
    var o: Byte by memoryAccess<Byte>(7)

    fun eval() {
        lib.evalFunctionHandle.invokeExact(state)
        println(dump())
    }

    fun initial() {
        lib.initialFunctionHandle.invokeExact(state)
    }

    fun final() {
        lib.finalFunctionHandle.invokeExact(state)
    }

    fun dump() = State(
        clk,
        reset,
        internal.clk,
        internal.reset,
        internal.reg,
        internal.o,
        o
    )

    companion object {
        //    "numStateBytes": 8,
        const val NUM_STATE_BYTES: Long = 8

        fun library(name: String, arena: Arena) = object : ModuleLibrary(
            name,
            arena,
            evalFnSym = "Dut_eval",
            //    "initialFnSym": "",
            initialFnSym = "",
            //    "finalFnSym": "",
            finalFnSym = ""
        ) {
            override val evalFunctionHandle: MethodHandle = functionHandle(evalFnSym)
            override val initialFunctionHandle: MethodHandle by lazy { stubFunctionHandle("initialFnSym is blank: '$initialFnSym'") }
            override val finalFunctionHandle: MethodHandle by lazy { stubFunctionHandle("finalFnSym is blank: '$finalFnSym'") }
        }

        fun instance(stateArena: Arena = Arena.ofAuto(), libName: String, libArena: Arena = Arena.ofAuto()) =
            Dut(stateArena.allocate((NUM_STATE_BYTES)), library(libName, libArena))
    }

}


abstract class ModuleLibrary(
    val name: String,
    val arena: Arena,
    val evalFnSym: String,
    val initialFnSym: String,
    val finalFnSym: String,
) {
    val libraryName: String = System.mapLibraryName(name)

    val symbolLookup: SymbolLookup = SymbolLookup
        .libraryLookup(libraryName, arena)
        .or(SymbolLookup.loaderLookup())
        .or(Linker.nativeLinker().defaultLookup())

    abstract val evalFunctionHandle: MethodHandle

    abstract val initialFunctionHandle: MethodHandle

    abstract val finalFunctionHandle: MethodHandle

    protected fun functionHandle(symbolName: String): MethodHandle {
        val symbol: MemorySegment = symbolLookup.find(symbolName).orElseThrow {
            UnsatisfiedLinkError("unresolved symbol: $symbolName")
        }
        val descriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        val handle = Linker.nativeLinker().downcallHandle(symbol, descriptor)
        return handle
    }

    protected fun stubFunctionHandle(reason: String): MethodHandle {
        TODO("Undefined symbol: $reason")
    }
}


abstract class Module(val name: String, val state: MemorySegment, val lib: ModuleLibrary, val numStateBytes: Long)

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
inline fun <reified T> Module.memoryAccess(offset: Long): MemoryAccessProperty<T> {
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

