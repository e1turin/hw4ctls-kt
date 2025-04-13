package io.github.e1turin.cirkt

import io.github.e1turin.cirkt.arcilator.StateFile
import io.github.e1turin.cirkt.model.ModelClassGenerator
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Paths

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
//            val json = this::class.java.getResource("model-states.json")?.readText()
            val json = File("src/jvmMain/resources/arcilator/model-states.json").readText()

            val models: StateFile = Json.decodeFromString(json)

            println(models[0])

            val gen = ModelClassGenerator(
                models[0],
                openModelClass = true,
                openLibraryClass = true,
                internalStateProjections = true,
                allStateProjectionsOpen = true,
                allStateProjectionsMutable = true
            )
            val fs = gen.generateFileSpec( "io.github.e1turin.cirkt.generated")
            // add source set in build.gradle.kts
            // "/absolute/path/to/build/generated/sources/cirkt/kotlin/main"
            fs.writeTo(Paths.get("src/jvmMain/kotlin"))

            playWithFFM()
        }
    }
}
