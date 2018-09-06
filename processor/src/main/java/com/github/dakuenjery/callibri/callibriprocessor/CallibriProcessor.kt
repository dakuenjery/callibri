package com.github.dakuenjery.callibri.callibriprocessor

import com.github.dakuenjery.callibri.annotations.LocalMethod
import com.github.dakuenjery.callibri.annotations.RemoteMethod
import com.squareup.kotlinpoet.*
import java.io.File
//import com.google.auto.service.AutoService
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.tools.Diagnostic


internal const val CORE_PACKAGE = "com.github.dakuenjery.callibri.core"
internal const val ROOT_PACKAGE = "com.github.dakuenjery.callibri"

internal const val GENERATED_PACKAGE = "callibri.generated"


class Factory(messager: Messager) {
    private val remoteSerializerProvider = PrimitiveArgsRemoteSerializerProvider()
    private val clientImplProvider = IpcClientImplProvider(remoteSerializerProvider)

    private val localSerializerProvider = PrimitiveArgsLocalSerializerProvider()
    private val serviceImplProvider = IpcServiceImplProvider(localSerializerProvider)

    private val remoteMethodTypeChecker = RemoteMethodTypeChecker(messager)
    private val localMethodTypeChecker = LocalMethodTypeChecker(messager)

    fun getSerializer(clazz: Class<out Annotation>): ArgsSerializerProvider {
        return when (clazz) {
            RemoteMethod::class.java -> remoteSerializerProvider
            LocalMethod::class.java -> localSerializerProvider
            else -> throw Exception()
        }
    }

    fun getImplProvider(clazz: Class<out Annotation>): ImplProvider {
        return when (clazz) {
            RemoteMethod::class.java -> clientImplProvider
            LocalMethod::class.java -> serviceImplProvider
            else -> throw Exception()
        }
    }

    fun getTypeChecker(clazz: Class<out Annotation>): TypeChecker {
        return when (clazz) {
            RemoteMethod::class.java -> remoteMethodTypeChecker
            LocalMethod::class.java -> localMethodTypeChecker
            else -> throw Exception()
        }
    }
}


//@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(CallibriProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
@SupportedAnnotationTypes(
        "com.github.dakuenjery.callibri.annotations.RemoteMethod",
        "com.github.dakuenjery.callibri.annotations.LocalMethod"
)
class CallibriProcessor : AbstractProcessor() {

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    private lateinit var factory: Factory

    override fun init(p0: ProcessingEnvironment) {
        super.init(p0)

        factory = Factory(p0.messager)
    }

    override fun process(elements: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val generatedSourcesRoot: String = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()
        if(generatedSourcesRoot.isEmpty()) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Can't find the target directory for generated Kotlin files.")
            return false
        }

        val file = File(generatedSourcesRoot).apply { mkdir() }

        for (clazz in listOf(RemoteMethod::class.java, LocalMethod::class.java)) {
            process(clazz, roundEnv, file)
        }
//
////        val elemsWithMethods =
////                getElementsWithProcessedMethods(RemoteMethod::class.java, roundEnv)
//
//        if (elemsWithMethods.isEmpty())
//            return true
//
//
//
//        val clientImpls = elemsWithMethods.map {
//            clientImplProvider.create(it.key, it.value)
//        }
//
//        val clientImplsFile = FileSpec.builder(GENERATED_PACKAGE, "ClientImpls")
//                .apply {
//                    clientImpls.forEach { addType(it) }
//                }
//                .build()
//
//        File(generatedSourcesRoot).apply {
//            mkdir()
//
////            remoteSerializerProvider.buildFile(GENERATED_PACKAGE).writeTo(this)
////            clientImplsFile.writeTo(this)
//        }

        return true
    }

    private fun process(clazz: Class<out Annotation>, roundEnv: RoundEnvironment, dir: File) {
        val objs = getElementsWithProcessedMethods(clazz, roundEnv)

        if (objs.isEmpty())
            return

        val serializerProvider = factory.getSerializer(clazz)
        val implProvider = factory.getImplProvider(clazz)

        // prepare serializers
        for (methodsList in objs.values)
            for (executableElement in methodsList)
                serializerProvider.prepare(executableElement)

        // prepare implementations
        for (entry in objs)
            implProvider.prepare(entry.key, entry.value)

        // write to file
        serializerProvider.buildFile(GENERATED_PACKAGE).writeTo(dir)
        implProvider.buildFile(GENERATED_PACKAGE, "${clazz.simpleName}Impls").writeTo(dir)
    }

    private fun getElementsWithProcessedMethods(clazz: Class<out Annotation>, roundEnv: RoundEnvironment): Map<Element, List<ExecutableElement>> {
        return roundEnv.getElementsAnnotatedWith(clazz)
                .filter {
                    factory.getTypeChecker(clazz).check(it)
                }
                .map {
                    it as ExecutableElement
                }
                .groupBy { it.enclosingElement }
    }
}

