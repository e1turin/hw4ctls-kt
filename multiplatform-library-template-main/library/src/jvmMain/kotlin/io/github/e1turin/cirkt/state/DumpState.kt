package io.github.e1turin.cirkt.state

interface DumpStateVisitor {
    fun dumpStateByte(name: String, value: Byte) {}
    fun dumpStateShort(name: String, value: Short) {}
    fun dumpStateInt(name: String, value: Int) {}
    fun dumpStateLong(name: String, value: Long) {}
    fun dumpStateFloat(name: String, value: Float) {}
    fun dumpStateDouble(name: String, value: Double) {}
    fun dumpStateBoolean(name: String, value: Boolean) {}
    fun dumpStateChar(name: String, value: Char) {}
    fun dumpStateString(name: String, value: String) {}
}

interface Dumpable {
    fun dumpTo(visitor: DumpStateVisitor)
}
