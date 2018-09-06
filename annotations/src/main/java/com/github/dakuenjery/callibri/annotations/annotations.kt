package com.github.dakuenjery.callibri.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class LocalMethod(val methodId: Int)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class RemoteMethod(val methodId: Int)