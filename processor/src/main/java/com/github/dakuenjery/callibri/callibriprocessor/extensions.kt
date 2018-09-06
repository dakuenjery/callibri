package com.github.dakuenjery.callibri.callibriprocessor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.type.TypeMirror
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.lang.model.element.ExecutableElement

private val primitiveTypesMap = mapOf(
        java.lang.Boolean::class.java.canonicalName to Boolean::class.qualifiedName,
        java.lang.Byte::class.java.canonicalName to Byte::class.qualifiedName,
        java.lang.Short::class.java.canonicalName to Short::class.qualifiedName,
        java.lang.Integer::class.java.canonicalName to Int::class.qualifiedName,
        java.lang.Float::class.java.canonicalName to Float::class.qualifiedName,
        java.lang.Double::class.java.canonicalName to Double::class.qualifiedName,
        java.lang.String::class.java.canonicalName to String::class.qualifiedName
)

fun TypeMirror.kotlinType(): TypeName {
    val typename = asTypeName()

    return when (typename) {
        is ParameterizedTypeName -> {
            var ktClass = typename.kotlinType() as ClassName
            val types = typename.typeArguments.map { it.kotlinType() }
            ktClass.parameterizedBy(*types.toTypedArray())
        }
        else -> typename.kotlinType()
    }
}

fun TypeName.kotlinType(): TypeName {
    val canonicalName = when (this) {
        is ParameterizedTypeName -> {
            this.toString().substringBefore('<')
        }
        else -> {
            this.toString()
        }
    }

    val type = primitiveTypesMap[canonicalName] ?: canonicalName
    val typePackage = type.substringBeforeLast('.')
    val typeName = type.substringAfterLast('.')
    return ClassName(typePackage, typeName)
}

fun TypeName.isPrimitive(): Boolean {
    val a = primitiveTypesMap.containsKey(this.toString())
    val b = primitiveTypesMap.containsValue(this.toString())
    return a || b
}