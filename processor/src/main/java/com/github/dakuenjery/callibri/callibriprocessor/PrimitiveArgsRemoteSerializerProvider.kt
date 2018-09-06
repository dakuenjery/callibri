package com.github.dakuenjery.callibri.callibriprocessor

import com.squareup.kotlinpoet.*
import javax.lang.model.element.VariableElement


private const val CLASSNAME = "PrimitiveArgsRemoteSerializer"

class PrimitiveArgsRemoteSerializerProvider : ArgsSerializerProvider(ClassName(CORE_PACKAGE, CLASSNAME)) {
    override fun createFunctions(params: List<VariableElement>, ret: TypeName): List<FunSpec> {
        return listOf(createArgumentsFunc(params), createResponseFunc(ret))
    }

    private fun createArgumentsFunc(params: List<VariableElement>): FunSpec {
        val bundleBuilder = PrimitiveBundleBuilder("bundle")

        val func = FunSpec.builder("setArguments")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("bundle", CLASSNAME_BUNDLE)
                .addParameter("args", ClassName("", "Any"), KModifier.VARARG)
                .addCode(
                        CodeBlock.builder().apply {
                            params.forEachIndexed { i, param ->
                                val varName = param.simpleName.toString()
                                val type = param.asType().kotlinType()
                                val value = "args[$i]"
                                bundleBuilder.putStatement(this, varName, type, value)
                            }
                        }.build()
                )
                .build()

        return func
    }

    private fun createResponseFunc(ret: TypeName): FunSpec {
        val bundleBuilder = PrimitiveBundleBuilder("bundle")

        val retWildcardType = (ret as ParameterizedTypeName).typeArguments[0].kotlinType()

        val func = FunSpec.builder("parseResponse")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("bundle", CLASSNAME_BUNDLE)
                .returns(retWildcardType)
                .addCode(
                        CodeBlock.builder().apply {
                            bundleBuilder.returnStatement(this, retWildcardType)
                        }.build()
                ).build()

        return func
    }
}

