// !LANGUAGE: +GenerateNullChecksForGenericTypeReturningFunctions
// TARGET_BACKEND: JVM
// IGNORE_BACKEND: JVM_IR
// WITH_RUNTIME

fun <T> foo(): T = null as T

fun box(): String {
    try {
        val xx = foo<String>()
    } catch (e: NullPointerException) {
        return "OK"
    }
    return "Fail: NullPointerException should have been thrown"
}
