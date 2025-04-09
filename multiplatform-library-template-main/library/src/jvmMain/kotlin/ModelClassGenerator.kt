package io.github.e1turin.cirkt.generator

import com.squareup.kotlinpoet.*
import io.github.e1turin.cirkt.ModelInfo
import io.github.e1turin.cirkt.StateInfo
import io.github.e1turin.cirkt.StateType
import java.nio.file.Paths

class ModelClassGenerator {
    fun generate(modelInfo: ModelInfo, outputDir: String) {
        val className = modelInfo.name.replaceFirstChar { it.uppercase() }
        val fileSpec = FileSpec.builder("io.github.e1turin.cirkt.generated", className)
            .addImport("java.lang.invoke", "MethodHandle") // Add this
            .addImport("io.github.e1turin.cirkt", "memoryAccess") // Add this
            .addType(generateClass(modelInfo))
            .build()
        fileSpec.writeTo(Paths.get(outputDir))
    }

    private fun generateClass(modelInfo: ModelInfo): TypeSpec {
        val className = modelInfo.name.replaceFirstChar { it.uppercase() }
        val (topLevelProperties, internalProperties) = modelInfo.states.partition {
            it.type == StateType.INPUT || it.type == StateType.OUTPUT
        }

        return TypeSpec.classBuilder(className)
            .addModifiers(KModifier.OPEN)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("state", MEMORY_SEGMENT)
                    .addParameter("lib", MODEL_LIBRARY)
                    .build()
            )
            .superclass(MODEL)
            .addSuperclassConstructorParameter("%S", modelInfo.name)
            .addSuperclassConstructorParameter("state")
            .addSuperclassConstructorParameter("lib")
            .addSuperclassConstructorParameter("%L", modelInfo.numStateBytes)
            .addProperties(generateProperties(topLevelProperties))
            .addType(generateInternalClass(internalProperties))
            .addType(generateStateDataClass(modelInfo)) // 1. State data class
            .addProperty( // 2. InternalStates instance
                PropertySpec.builder("internal", ClassName("", "InternalStates"))
                    .initializer("InternalStates()")
                    .build()
            )
            .addFunctions(listOf( // Add missing functions
                generateEvalFunction(),
                generateInitialFunction(),
                generateFinalFunction()
            ))
            .addFunction(generateDumpFunction(modelInfo))
            .addType(generateCompanionObject(modelInfo))
            .build()
    }

    private fun generateProperties(states: List<StateInfo>): List<PropertySpec> {
        return states.map { state ->
            PropertySpec.builder(state.name.decapitalize(), getTypeForNumBits(state.numBits))
                .mutable(state.type == StateType.INPUT)
                .delegate(
                    CodeBlock.of("memoryAccess<%T>(%L, %T.%L)",
                        getTypeForNumBits(state.numBits),
                        state.offset,
                        StateType::class,
                        state.type.name
                    )
                )
                .build()
        }
    }

    private fun generateStateDataClass(modelInfo: ModelInfo): TypeSpec {
        val properties = modelInfo.states.map { state ->
            val propName = when (state.type) {
                StateType.INPUT, StateType.OUTPUT -> state.name
                else -> "internal${state.name.replaceFirstChar { it.uppercase() }}"
            }
            PropertySpec.builder(propName, getTypeForNumBits(state.numBits))
                .initializer(propName)
                .build()
        }

        return TypeSpec.classBuilder("State")
            .addModifiers(KModifier.DATA)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameters(
                        properties.map { prop ->
                            ParameterSpec.builder(prop.name, prop.type).build()
                        }
                    )
                    .build()
            )
            .addProperties(properties)
            .build()
    }

    private fun generateInternalClass(states: List<StateInfo>): TypeSpec {
        return TypeSpec.classBuilder("InternalStates")
            .addModifiers(KModifier.INNER)
            .addProperties(
                states.map { state ->
                    PropertySpec.builder(state.name.decapitalize(), getTypeForNumBits(state.numBits))
                        .mutable(true)
                        .delegate(
                            CodeBlock.of("memoryAccess<%T>(%L, %T.%L)",
                                getTypeForNumBits(state.numBits),
                                state.offset,
                                StateType::class,
                                state.type.name
                            )
                        )
                        .build()
                }
            )
            .build()
    }

    private fun generateDumpFunction(modelInfo: ModelInfo): FunSpec {
        val params = modelInfo.states.map { state ->
            when (state.type) {
                StateType.INPUT, StateType.OUTPUT -> state.name
                else -> "internal.${state.name}"
            }
        }
        return FunSpec.builder("dump")
            .returns(ClassName("", "State"))
            .addStatement("return State(%L)", params.joinToString())
            .build()
    }

    private fun generateCompanionObject(modelInfo: ModelInfo): TypeSpec {
        return TypeSpec.companionObjectBuilder()
            .addProperty(
                PropertySpec.builder("NUM_STATE_BYTES", LONG)
                    .addModifiers(KModifier.CONST)
                    .initializer("%LL", modelInfo.numStateBytes)
                    .build()
            )
            .addFunction(generateLibraryFunction(modelInfo))
            .addFunction(generateInstanceFunction(modelInfo))
            .build()
    }

    private fun generateLibraryFunction(modelInfo: ModelInfo): FunSpec {
        return FunSpec.builder("library")
            .addParameter("name", STRING)
            .addParameter("arena", ARENA)
            .returns(MODEL_LIBRARY)
            .addCode(
                CodeBlock.builder()
                    .add("return object : ModelLibrary(name, arena, %S, %S, %S) {\n",
                        "${modelInfo.name}_eval",
                        modelInfo.initialFnSym,
                        modelInfo.finalFnSym
                    )
                    .indent()
                    .addStatement("override val evalFunctionHandle: MethodHandle = functionHandle(evalFnSym)")
                    .beginControlFlow("override val initialFunctionHandle: MethodHandle by lazy")
                    .addStatement("stubFunctionHandle(%S)", "initialFnSym is blank: '\$initialFnSym'")
                    .endControlFlow()
                    .beginControlFlow("override val finalFunctionHandle: MethodHandle by lazy")
                    .addStatement("stubFunctionHandle(%S)", "finalFnSym is blank: '\$finalFnSym'")
                    .endControlFlow()
                    .unindent()
                    .add("}\n")
                    .build()
            )
            .build()
    }

    private fun generateInstanceFunction(modelInfo: ModelInfo): FunSpec {
        return FunSpec.builder("instance")
            .addParameter(
                ParameterSpec.builder("stateArena", ARENA)
                    .defaultValue("%T.ofAuto()", ARENA)
                    .build()
            )
            .addParameter("libName", STRING)
            .addParameter(
                ParameterSpec.builder("libArena", ARENA)
                    .defaultValue("%T.ofAuto()", ARENA)
                    .build()
            )
            .returns(ClassName("", modelInfo.name))
            .addStatement(
                "return %T(stateArena.allocate(NUM_STATE_BYTES), library(libName, libArena))",
                ClassName("", modelInfo.name)
            )
            .build()
    }

    private fun generateEvalFunction(): FunSpec =
        FunSpec.builder("eval")
            .addStatement("lib.evalFunctionHandle.invokeExact(state)")
            .build()

    private fun generateInitialFunction(): FunSpec =
        FunSpec.builder("initial")
            .addStatement("lib.initialFunctionHandle.invokeExact(state)")
            .build()

    private fun generateFinalFunction(): FunSpec =
        FunSpec.builder("final")
            .addStatement("lib.finalFunctionHandle.invokeExact(state)")
            .build()

    private fun getTypeForNumBits(numBits: UInt): TypeName = when {
        numBits <= 8u -> BYTE
        numBits <= 16u -> SHORT
        numBits <= 32u -> INT
        numBits <= 64u -> LONG
        else -> throw IllegalArgumentException("Unsupported numBits: $numBits")
    }

    companion object {
        private val MEMORY_SEGMENT = ClassName("java.lang.foreign", "MemorySegment")
        private val MODEL_LIBRARY = ClassName("io.github.e1turin.cirkt", "ModelLibrary")
        private val MODEL = ClassName("io.github.e1turin.cirkt", "Model")
        private val ARENA = ClassName("java.lang.foreign", "Arena")
        private val METHOD_HANDLE = ClassName("java.lang.invoke", "MethodHandle")
        private val STRING = String::class.asClassName()
        private val LONG = Long::class.asClassName()
        private val BYTE = Byte::class.asClassName()
        private val SHORT = Short::class.asClassName()
        private val INT = Int::class.asClassName()
    }
}
