package com.github.dakuenjery.callibri.callibriprocessor

import com.squareup.kotlinpoet.asTypeName
import java.lang.StringBuilder
import java.util.zip.CRC32
import javax.lang.model.element.ExecutableElement
import kotlin.reflect.KClass

class Identifier(klass: KClass<*>, method: ExecutableElement) {

    private val prefix: String = klass.simpleName ?: ""
    val fullIdentifier = getFullIdentifier(method)
    val hash = getHashIdentifier(method)

    private fun getFullIdentifier(method: ExecutableElement): String {
        val argTypes = method.parameters.map {
            it.asType().asTypeName().toString()
        }

        val retType = method.returnType.asTypeName().toString()

        val str = StringBuilder().apply {
            append(prefix)
            append("@")
            append(argTypes.joinToString(";"))
            append("_")
            append(retType)
        }.toString()

        return str
    }

    private fun getHashIdentifier(method: ExecutableElement): Long {
        val crc32 = CRC32()
        crc32.update(getFullIdentifier(method).toByteArray())
        return crc32.value
    }

    override fun hashCode(): Int {
        return hash.toInt()
    }

    override fun toString(): String {
        return hash.toString(36)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        return hash == (other as Identifier).hash
    }
}