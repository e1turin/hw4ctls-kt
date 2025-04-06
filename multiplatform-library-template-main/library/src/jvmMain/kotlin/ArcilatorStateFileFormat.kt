package io.github.e1turin.cirkt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


// Data structures used in Arcilator state file https://github.com/llvm/circt/blob/main/include/circt/Dialect/Arc/ModelInfo.h
//
// /// permalink: https://github.com/llvm/circt/blob/218c1857b44a7d8a54d9005135bf993d8893854c/include/circt/Dialect/Arc/ModelInfo.h#L25C1-L32C3
// struct StateInfo {
//   enum Type { Input, Output, Register, Memory, Wire } type;
//   std::string name;
//   unsigned offset;
//   unsigned numBits;
//   unsigned memoryStride = 0; // byte separation between memory words
//   unsigned memoryDepth = 0;  // number of words in a memory
// };
//
// /// https://github.com/llvm/circt/blob/218c1857b44a7d8a54d9005135bf993d8893854c/include/circt/Dialect/Arc/ModelInfo.h#L35C1-L49C3
// struct ModelInfo {
//   std::string name;
//   size_t numStateBytes;
//   llvm::SmallVector<StateInfo> states;
//   // ...
// };
//

// Types of observable objects
// serial names are taken from json encoder: https://github.com/llvm/circt/blob/d675c243c04339563517de1717dacbe3aa8309d5/lib/Dialect/Arc/ModelInfo.cpp#L130C3-L171C6
@Serializable
enum class StateType {
    @SerialName("input")
    INPUT,
    @SerialName("output")
    OUTPUT,
    @SerialName("register")
    REGISTER,
    @SerialName("memory")
    MEMORY,
    @SerialName("wire")
    WIRE
}

@Serializable
data class StateInfo(
    val type: StateType,
    val name: String,
    val offset: UInt,
    val numBits: UInt,
    val memoryStrides: UInt = 0u,
    val memoryDepth: UInt = 0u,
)

@Serializable
data class ModelInfo(
    val name: String,
    val numStateBytes: ULong,
    val initialFnSym: String,
    val finalFnSym: String,
    val states: List<StateInfo>
)

typealias StateFile = List<ModelInfo>

