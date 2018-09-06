package com.github.dakuenjery.callibri.callibriprocessor

import com.squareup.kotlinpoet.*
import javax.lang.model.element.Name
import javax.lang.model.element.VariableElement

class PrimitiveBundleBuilder(private val bundleVarName: String) {
    fun putStatement(builder: CodeBlock.Builder, varName: String, typeName: TypeName, valueArg: String): CodeBlock.Builder {
        val type = (typeName.kotlinType() as ClassName).simpleName

//        val statement = "$bundleVarName.put$type(\"$varName\", $valueArg as $type)"
        builder.addStatement("$bundleVarName.put$type(%S, $valueArg as %T)", varName, ClassName("", type))

        return builder
    }

    fun returnStatement(builder: CodeBlock.Builder, typeName: TypeName): CodeBlock.Builder {
        val type = (typeName.kotlinType() as ClassName).simpleName
        builder.addStatement("return $bundleVarName.get$type(%S)", null)
        return builder
    }

    fun varStatement(builder: CodeBlock.Builder, varName: String, typeName: TypeName): CodeBlock.Builder {
        val type = (typeName.kotlinType() as ClassName).simpleName
        builder.addStatement("val %L = $bundleVarName.get$type(%S)", varName, varName)
        return builder
    }

    private fun getStatement(builder: CodeBlock.Builder, prefix: String, varName: String?, typeName: TypeName): CodeBlock.Builder {
        val type = (typeName.kotlinType() as ClassName).simpleName

//        val statement = "$prefix $bundleVarName.get$type(\"$varName\")"
        builder.addStatement("$prefix $bundleVarName.get$type(%L)", varName)

        return builder
    }
}