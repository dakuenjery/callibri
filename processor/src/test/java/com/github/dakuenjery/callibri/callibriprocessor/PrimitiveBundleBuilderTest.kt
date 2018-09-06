package com.github.dakuenjery.callibri.callibriprocessor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName

internal class PrimitiveBundleBuilderTest {

    private val generator = PrimitiveBundleBuilder(bundleArg)

    @ParameterizedTest
    @MethodSource("putStatements")
    fun putStatement(className: TypeName, output: String) {
        val code = generator.putStatement(CodeBlock.builder(), varName, className, varValue).build().toString().trim()
        assertEquals(output, code)
    }

    @ParameterizedTest
    @MethodSource("returnStatements")
    fun returnStatement(className: TypeName, output: String) {
        val code = generator.returnStatement(CodeBlock.builder(), className).build().toString().trim()
        assertEquals(output, code)
    }

    @ParameterizedTest
    @MethodSource("varStatements")
    fun varStatement(className: TypeName, output: String) {
        val code = generator.varStatement(CodeBlock.builder(), varName, className).build().toString().trim()
        assertEquals(output, code)
    }

    companion object {
        val bundleArg = "bundle"
        val varName = "var1"
        val varValue = "value"

        @JvmStatic fun putStatements() = listOf(
                Arguments.of(ClassName("java.lang", "String"), """bundle.putString("var1", value as String)"""),
                Arguments.of(ClassName("java.lang", "Double"), """bundle.putDouble("var1", value as Double)"""),
                Arguments.of(ClassName("kotlin", "Int"), """bundle.putInt("var1", value as Int)"""),
                Arguments.of(ClassName("java.lang", "Integer"), """bundle.putInt("var1", value as Int)""")
        )


        @JvmStatic fun returnStatements() = listOf(
                Arguments.of(ClassName("java.lang", "String"), """return bundle.getString(null)"""),
                Arguments.of(ClassName("java.lang", "Double"), """return bundle.getDouble(null)"""),
                Arguments.of(ClassName("kotlin", "Int"), """return bundle.getInt(null)"""),
                Arguments.of(ClassName("java.lang", "Integer"), """return bundle.getInt(null)""")
        )

        @JvmStatic fun varStatements() = listOf(
                Arguments.of(ClassName("java.lang", "String"), """val var1 = bundle.getString("var1")"""),
                Arguments.of(ClassName("java.lang", "Double"), """val var1 = bundle.getDouble("var1")"""),
                Arguments.of(ClassName("kotlin", "Int"), """val var1 = bundle.getInt("var1")"""),
                Arguments.of(ClassName("java.lang", "Integer"), """val var1 = bundle.getInt("var1")""")
        )
    }
}