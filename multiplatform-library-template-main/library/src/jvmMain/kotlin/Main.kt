package io.github.e1turin.cirkt

import kotlinx.serialization.json.Json
import java.io.File

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
//            val json = this::class.java.getResource("model-states.json")?.readText()
            val json = File("src/jvmMain/resources/arcilator/model-states.json").readText()

            val models: StateFile = Json.decodeFromString(json)

            println(models[0])
        }
    }
}
