package com.github.dakuenjery.callibri.callibriprocessor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.jvm.jvmWildcard
import com.sun.org.apache.xpath.internal.operations.Bool
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.type.TypeKind
import javax.tools.Diagnostic

abstract class TypeChecker(val messager: Messager) {
    open fun check(element: Element): Boolean {
        if (element.kind != ElementKind.METHOD) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Can be applied to functions only. Element: $element", element)
            return false
        }

        val element = element as ExecutableElement

        return listOf(checkParams(element), checkReturn(element), checkModifiers(element)).all { it }
    }

    protected abstract fun checkParams(method: ExecutableElement): Boolean
    protected abstract fun checkReturn(method: ExecutableElement): Boolean
    protected abstract fun checkModifiers(method: ExecutableElement): Boolean
}


class RemoteMethodTypeChecker(messager: Messager) : TypeChecker(messager) {

    override fun checkParams(method: ExecutableElement): Boolean {
        val params = method.parameters

        return params.map {
            it.asType().asTypeName().isPrimitive().apply {
                if (!this)
                    messager.printMessage(Diagnostic.Kind.ERROR, "Only primitive types and string are supported. ${it.simpleName} is not primitive type", method)
            }
        }.all { it }
    }

    override fun checkReturn(method: ExecutableElement): Boolean {
        val type = method.returnType.asTypeName()

        if (type !is ParameterizedTypeName ||
                type.rawType.canonicalName != "$CORE_PACKAGE.Call" ||
                type.typeArguments.any { !it.isPrimitive() })
        {
            val msg = "Only Call parametrized primitive type or string are supported. Type $type not supported"
            messager.printMessage(Diagnostic.Kind.ERROR, msg, method)
            return false
        }

        return true
    }

    override fun checkModifiers(method: ExecutableElement): Boolean {
        return method.modifiers.contains(Modifier.ABSTRACT).apply {
            if (!this)
                messager.printMessage(Diagnostic.Kind.ERROR, "Remote method must be abstract", method)
        }
    }
}

class LocalMethodTypeChecker(messager: Messager) : TypeChecker(messager) {

    override fun checkParams(method: ExecutableElement): Boolean {
        val params = method.parameters

        var b = true

        params.forEachIndexed { i, variable ->
            val type = variable.asType().asTypeName()

            if (!type.isPrimitive()) {
                if ((type as ClassName).simpleName == "ResultReceiver") {
                    if (i != 0) {
                        b = false
                        messager.printMessage(Diagnostic.Kind.ERROR, "Result Receiver argument must be first", method)
                    }
                } else {
                    b = false
                    messager.printMessage(Diagnostic.Kind.ERROR, "Only primitive types and string are supported. ${type.simpleName} is not primitive type", method)
                }
            }
        }

        return b
    }

    override fun checkReturn(method: ExecutableElement): Boolean {
        return (method.returnType.kind == TypeKind.VOID).apply {
            if (!this)
                messager.printMessage(Diagnostic.Kind.ERROR, "Return type must be Void", method)
        }
    }

    override fun checkModifiers(method: ExecutableElement): Boolean {
        return !method.modifiers.contains(Modifier.ABSTRACT).apply {
            if (this)
                messager.printMessage(Diagnostic.Kind.ERROR, "Local method cannot be abstract", method)
        }
    }
}