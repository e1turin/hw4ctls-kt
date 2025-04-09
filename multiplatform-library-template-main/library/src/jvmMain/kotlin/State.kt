package io.github.e1turin.cirkt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
