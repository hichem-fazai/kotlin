// !LANGUAGE: +InlineClasses
// WITH_COROUTINES
// FILE: a.kt

package a

var result = ""

inline class P(val value: String)

suspend fun foo(p: P = P("OK")) {
    result = p.value
}

// FILE: b.kt

import kotlin.coroutines.*
import helpers.*
import a.*

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

fun box(): String {
    builder {
        foo()
    }
    return result
}
