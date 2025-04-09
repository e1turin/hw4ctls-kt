package io.github.e1turin.cirkt

import java.lang.foreign.*
import java.lang.invoke.MethodHandle

abstract class ModelLibrary(
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
