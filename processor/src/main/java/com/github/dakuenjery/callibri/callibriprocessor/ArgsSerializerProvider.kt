package com.github.dakuenjery.callibri.callibriprocessor

import com.squareup.kotlinpoet.*
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.TypeKind
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.jvm.jvmWildcard
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror


internal val CLASSNAME_BUNDLE = ClassName("android.os", "Bundle")

abstract class ArgsSerializerProvider(val extendedClassName: ClassName) {
    protected val serializers = mutableMapOf<Identifier, TypeSpec>()
    protected val typeSpecs: List<TypeSpec> get() = serializers.values.toList()

    fun getSerializer(method: ExecutableElement): TypeSpec {
        val id = Identifier(this::class, method)

        return serializers.getOrPut(id) {
            createSerializer(method, id)
        }
    }

    fun prepare(method: ExecutableElement) {
        getSerializer(method)
    }

    open fun classname(method: ExecutableElement, identifier: Identifier): String {
        return "${extendedClassName.simpleName}_$identifier"
    }

    open fun superclass(method: ExecutableElement): TypeName {
        return if (method.returnType.kind == TypeKind.VOID)
            extendedClassName
        else
            extendedClassName.parameterizedBy(*getWildcardKotlinType(method.returnType))
    }

    abstract fun createFunctions(params: List<VariableElement>, ret: TypeName): List<FunSpec>

    private fun createSerializer(method: ExecutableElement, identifier: Identifier): TypeSpec {
        return TypeSpec.classBuilder(classname(method, identifier))
                .superclass(superclass(method))
                .addKdoc("${identifier.fullIdentifier}")
                .apply {
                    for (funSpec in createFunctions(method.parameters, method.returnType.kotlinType())) {
                        addFunction(funSpec)
                    }
                }
                .build()
    }

    fun buildFile(packageName: String): FileSpec {
        val file = FileSpec.builder(packageName, "${extendedClassName.simpleName}Impls")
                .apply {
                    for (spec in typeSpecs) {
                        addType(spec)
                    }
                }.build()

        return file
    }

    private fun getWildcardKotlinType(typeMirror: TypeMirror): Array<TypeName> {
        return (typeMirror.asTypeName().jvmWildcard() as ParameterizedTypeName).typeArguments.map {
            it.kotlinType()
        }.toTypedArray()
    }
}