package io.github.e1turin.cirkt.generator

import com.squareup.kotlinpoet.*
import io.github.e1turin.cirkt.Model
import io.github.e1turin.cirkt.ModelLibrary
import io.github.e1turin.cirkt.arcilator.ModelInfo
import io.github.e1turin.cirkt.arcilator.StateInfo
import io.github.e1turin.cirkt.state.StateProjectionType
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.invoke.MethodHandle
import java.nio.file.Paths

fun generate(modelInfo: ModelInfo, outputDir: String, packageName: String = "") {
    val modelName = modelInfo.name.replaceFirstChar { it.uppercase() }
    val modelLibraryName = "${modelName}Library"

    val companion = TypeSpec.companionObjectBuilder()
        .addProperty(
            PropertySpec.builder("MODEL_NAME", String::class)
                .addModifiers(KModifier.CONST)
                .initializer("%S", modelInfo.name)
                .build()
        )
        .addProperty(
            PropertySpec.builder("NUM_STATE_BYTES", Long::class)
                .addModifiers(KModifier.CONST)
                .initializer("%LL", modelInfo.numStateBytes)
                .build()
        )
        .addFunction(
            FunSpec.builder("instance")
                .addParameter(ParameterSpec.builder("arena", Arena::class).build())
                .addParameter(ParameterSpec.builder("libraryName", String::class).build())
                .addParameter(
                    ParameterSpec.builder("libraryArena", Arena::class)
                        .defaultValue("%L", "Arena.ofAuto()")
                        .build()
                )
                .returns(
                    ClassName(packageName, modelName),
                )
                .addStatement("return ${modelName}(arena.allocate(NUM_STATE_BYTES), $modelLibraryName(libraryName, libraryArena))")
                .build()
        )
        .build()

    val modelClass = TypeSpec.classBuilder(modelName)
        .addModifiers(KModifier.OPEN)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter("state", MemorySegment::class)
                .addParameter("lib", ModelLibrary::class)
                .build()
        )
        .superclass(Model::class).apply {
            addSuperclassConstructorParameter("%L", "MODEL_NAME")
            addSuperclassConstructorParameter("state")
            addSuperclassConstructorParameter("lib")
            addSuperclassConstructorParameter("%L", "NUM_STATE_BYTES")
        }
        .addProperties(modelInfo.states.map { stateProjectionPropSpec(it) })
        .addFunctions(
            listOf(
                evalFunctionSpec(),
                initialFunctionSpec(),
                finalFunctionSpec()
            )
        )
        .addType(companion)
        .build()

    val modelLibraryClass = TypeSpec.classBuilder(modelLibraryName)
        .addModifiers(KModifier.OPEN)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter("name", String::class)
                .addParameter(
                    ParameterSpec.builder("arena", Arena::class)
                        .defaultValue("%L", "Arena.ofAuto()")
                        .build()
                )
                .build()
        )
        .superclass(ModelLibrary::class).apply {
            addSuperclassConstructorParameter("name")
            addSuperclassConstructorParameter("arena")
            addSuperclassConstructorParameter("\"${modelInfo.name}_eval\"")
            addSuperclassConstructorParameter("\"${modelInfo.initialFnSym}\"")
            addSuperclassConstructorParameter("\"${modelInfo.finalFnSym}\"")
        }.addProperty(
            PropertySpec.builder("evalFunctionHandle", MethodHandle::class)
                .initializer(CodeBlock.builder().addStatement("%L", "functionHandle(evalFnSym)").build())
                .addModifiers(KModifier.OVERRIDE)
                .build()
        )
        .addProperty(
            PropertySpec.builder("initialFunctionHandle", MethodHandle::class)
                .apply {
                    if (modelInfo.initialFnSym.isNotEmpty()) {
                        initializer(
                            CodeBlock.builder().addStatement("%L", "functionHandle(initialFnSym)").build()
                        )
                    } else {
                        getter(
                            FunSpec.getterBuilder()
                                .addCode(
                                    CodeBlock.builder()
                                        .add("throw NotImplementedError(\"No such symbol '\$initialFnSym'\")").build()
                                )
                                .build()
                        )
                    }
                }
                .addModifiers(KModifier.OVERRIDE)
                .build()
        )
        .addProperty(
            PropertySpec.builder("finalFunctionHandle", MethodHandle::class)
                .apply {
                    if (modelInfo.finalFnSym.isNotEmpty()) {
                        initializer(
                            CodeBlock.builder().addStatement("%L", "functionHandle(finalFnSym)").build()
                        )
                    } else {
                        getter(
                            FunSpec.getterBuilder()
                                .addCode(
                                    CodeBlock.builder()
                                        .add("throw NotImplementedError(\"No such symbol '\$finalFnSym'\")").build()
                                )
                                .build()
                        )
                    }
                }
                .addModifiers(KModifier.OVERRIDE)
                .build()
        )
        .build()

    val className = modelInfo.name.replaceFirstChar { it.uppercase() }
    val fileSpec = FileSpec.builder(packageName, className)
        .addType(modelClass)
        .addType(modelLibraryClass)
        .build()
    fileSpec.writeTo(Paths.get(outputDir))
}

private fun stateProjectionPropSpec(state: StateInfo): PropertySpec {
    val stateProjectionName = if (state.type in listOf(StateProjectionType.INPUT, StateProjectionType.OUTPUT)) {
        state.name.replaceFirstChar { it.lowercase() }
    } else {
        "internal" + state.name.replaceFirstChar { it.uppercase() }
    }
    val delegate = when (state.type) {
        StateProjectionType.INPUT -> STATE_DELEGATE_INPUT
        StateProjectionType.OUTPUT -> STATE_DELEGATE_OUTPUT
        StateProjectionType.REGISTER -> STATE_DELEGATE_REGISTER
        StateProjectionType.MEMORY -> STATE_DELEGATE_MEMORY
        StateProjectionType.WIRE -> STATE_DELEGATE_WIRE
    }
    return PropertySpec.builder(stateProjectionName, getTypeForNumBits(state.numBits))
        .addModifiers(KModifier.OPEN)
        .mutable(state.type == StateProjectionType.INPUT)
        .delegate(
            CodeBlock.of(
                "%M<%T>(%L)",
                delegate,
                getTypeForNumBits(state.numBits),
                state.offset,
            )
        )
        .build()
}

private fun evalFunctionSpec(): FunSpec =
    FunSpec.builder("eval")
        .addStatement("lib.evalFunctionHandle.invokeExact(state)")
        .build()

private fun initialFunctionSpec(): FunSpec =
    FunSpec.builder("initial")
        .addStatement("lib.initialFunctionHandle.invokeExact(state)")
        .build()

private fun finalFunctionSpec(): FunSpec =
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

private val STATE_DELEGATE_INPUT = MemberName("io.github.e1turin.cirkt.state", "input")
private val STATE_DELEGATE_OUTPUT = MemberName("io.github.e1turin.cirkt.state", "output")
private val STATE_DELEGATE_REGISTER = MemberName("io.github.e1turin.cirkt.state", "register")
private val STATE_DELEGATE_MEMORY = MemberName("io.github.e1turin.cirkt.state", "memory")
private val STATE_DELEGATE_WIRE = MemberName("io.github.e1turin.cirkt.state", "wire")
