package io.github.e1turin.cirkt

import io.github.e1turin.cirkt.arcilator.StateFile
import io.github.e1turin.cirkt.generator.generate
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

            generate(models[0], "src/jvmMain/kotlin", "io.github.e1turin.cirkt.generated")

            playWithFFM()
        }
    }
}
