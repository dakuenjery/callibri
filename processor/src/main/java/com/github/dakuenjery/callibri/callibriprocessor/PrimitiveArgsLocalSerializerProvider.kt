package com.github.dakuenjery.callibri.callibriprocessor

import com.squareup.kotlinpoet.*
import javax.lang.model.element.VariableElement
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

private const val CLASSNAME = "PrimitiveArgsLocalSerializer"

class PrimitiveArgsLocalSerializerProvider : ArgsSerializerProvider(ClassName(CORE_PACKAGE, CLASSNAME)) {
    override fun createFunctions(params: List<VariableElement>, ret: TypeName): List<FunSpec> {
        val bundleBuilder = PrimitiveBundleBuilder("bundle")

        val params = params.subList(1, params.size)

        val func = FunSpec.builder("getArguments")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("bundle", CLASSNAME_BUNDLE)
                .returns(ClassName("", "Array").parameterizedBy(ClassName("", "Any")))
                .addCode(
                        CodeBlock.builder().apply {
                            params.forEachIndexed { i, param ->
                                val name = param.simpleName.toString()
                                val type = param.asType().kotlinType()
                                bundleBuilder.varStatement(this, name, type)
                            }
                        }.addStatement("return arrayOf(%L)", params.joinToString { it.simpleName.toString() })
                                .build()
                ).build()

        return listOf(func)
    }
}