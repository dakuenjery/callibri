package com.github.dakuenjery.callibri.callibriprocessor

import com.github.dakuenjery.callibri.annotations.LocalMethod
import com.github.dakuenjery.callibri.annotations.RemoteMethod
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement

abstract class ImplProvider(val serialiser: ArgsSerializerProvider) {
    private val impls = mutableListOf<TypeSpec>()

    fun prepare(element: Element, methods: List<ExecutableElement>) {
        val spec = TypeSpec.classBuilder("${element.simpleName}Impl")
                .superclass(element.asType().asTypeName())
                .apply { process(this, element, methods) }
                .build()

        impls.add(spec)
    }

    abstract fun process(builder: TypeSpec.Builder, element: Element, methods: List<ExecutableElement>)

    fun buildFile(packageName: String, fileName: String): FileSpec {
        val file = FileSpec.builder(packageName, fileName)
                .apply {
                    for (spec in impls) {
                        addType(spec)
                    }
                }.build()

        return file
    }
}

class IpcClientImplProvider(serialiser: ArgsSerializerProvider) : ImplProvider(serialiser) {
    override fun process(builder: TypeSpec.Builder, element: Element, methods: List<ExecutableElement>) {
        builder.apply {
            methods.forEach { overrideRemoteMethod(it) }
        }
        builder.addProperty(
                PropertySpec.builder("methodWrapperFactory", ClassName(ROOT_PACKAGE, "RemoteMethodWrapperFactory"), KModifier.OVERRIDE)
                        .initializer("%L", createRemoteMethodWrapperFactory(methods))
                        .build()
        )
    }

    private fun TypeSpec.Builder.overrideRemoteMethod(method: ExecutableElement): TypeSpec.Builder {
        val methodId = method.getAnnotation(RemoteMethod::class.java).methodId
        val params = method.parameters.joinToString { it.simpleName.toString() }

        val func = FunSpec.builder(method.simpleName.toString())
                .addModifiers(KModifier.OVERRIDE)
                .apply {
                    for (param in method.parameters) {
                        addParameter(param.simpleName.toString(), param.asType().kotlinType())
                    }
                }
                .returns(method.returnType.kotlinType())
                .addCode("return remoteInvoke(%L, $params)", methodId)
                .build()


        addFunction(func)

        return this
    }

    private fun createRemoteMethodWrapperFactory(methods: List<ExecutableElement>): TypeSpec {
        val type = TypeSpec.anonymousClassBuilder()
                .addSuperinterface(ClassName(ROOT_PACKAGE, "RemoteMethodWrapperFactory"))
                .addFunction(
                        FunSpec.builder("createMethodWrapper")
                                .addModifiers(KModifier.OVERRIDE)
                                .addParameter("methodId", Int::class.java)
                                .returns(ClassName(CORE_PACKAGE, "PrimitiveArgsRemoteSerializer").parameterizedBy(WildcardTypeName.STAR))
                                .addCode(
                                        CodeBlock.builder()
                                                .beginControlFlow("return when (methodId)")
                                                .apply {
                                                    methods.forEach {
                                                        val methodId = it.getAnnotation(RemoteMethod::class.java).methodId
                                                        val serializer = serialiser.getSerializer(it)

                                                        addStatement("%L -> %T()", methodId, ClassName("", serializer.name!!))
                                                    }
                                                }
                                                .addStatement("else -> throw Exception()")
                                                .endControlFlow()
                                                .build()
                                ).build()
                ).build()

        return type
    }
}

class IpcServiceImplProvider(serialiser: ArgsSerializerProvider) : ImplProvider(serialiser) {
    override fun process(builder: TypeSpec.Builder, element: Element, methods: List<ExecutableElement>) {
        builder.overrideHandleMessage(methods)
                .addProperty(
                        PropertySpec.builder("serializerFactory", ClassName(ROOT_PACKAGE, "LocalSerializerFactory"), KModifier.OVERRIDE)
                                .initializer("%L", createLocalMethodWrapperFactory(methods))
                                .build()
                )
    }

    private fun TypeSpec.Builder.overrideHandleMessage(methods: List<ExecutableElement>): TypeSpec.Builder {
        val func = FunSpec.builder("handleMessage")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("msgId", Int::class)
                .addParameter("receiver", ClassName("android.os", "ResultReceiver"))
                .addParameter("args", ClassName("", "Any"), KModifier.VARARG)
                .addCode(
                        CodeBlock.Builder()
                                .beginControlFlow("when (msgId)")
                                .apply {
                                    for (method in methods) {
                                        val methodId = method.getAnnotation(LocalMethod::class.java).methodId
                                        addStatement("%L -> %L(receiver, %L)", methodId, method.simpleName, method.toParamsLiteral("args"))
                                    }
                                }
                                .endControlFlow()
                                .build()
                ).build()

        addFunction(func)

        return this
    }

    private fun ExecutableElement.toParamsLiteral(paramArrName: String): String {
        return this.parameters.subList(1, parameters.size).mapIndexed { i, variable ->
            Pair(i, variable.asType().kotlinType().toString())
        }.joinToString {
            "args[${it.first}] as ${it.second}"
        }
    }

    private fun createLocalMethodWrapperFactory(methods: List<ExecutableElement>): TypeSpec {
        val type = TypeSpec.anonymousClassBuilder()
                .addSuperinterface(ClassName(ROOT_PACKAGE, "LocalSerializerFactory"))
                .addFunction(
                        FunSpec.builder("createSerializer")
                                .addModifiers(KModifier.OVERRIDE)
                                .addParameter("methodId", Int::class.java)
                                .returns(ClassName(CORE_PACKAGE, "PrimitiveArgsLocalSerializer"))
                                .addCode(
                                        CodeBlock.builder()
                                                .beginControlFlow("return when (methodId)")
                                                .apply {
                                                    methods.forEach {
                                                        val methodId = it.getAnnotation(LocalMethod::class.java).methodId
                                                        val serializer = serialiser.getSerializer(it)

                                                        addStatement("%L -> %T()", methodId, ClassName("", serializer.name!!))
                                                    }
                                                }
                                                .addStatement("else -> throw Exception()")
                                                .endControlFlow()
                                                .build()
                                ).build()
                ).build()

        return type
    }
}