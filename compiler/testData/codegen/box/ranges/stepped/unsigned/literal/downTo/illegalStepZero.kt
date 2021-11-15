// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
// KT-34166: Translation of loop over literal completely removes the validation of step
// DONT_TARGET_EXACT_BACKEND: JS
import kotlin.test.*

fun box(): String {
    assertFailsWith<IllegalArgumentException> {
        for (i in 7u downTo 1u step 0) {
        }
    }

    assertFailsWith<IllegalArgumentException> {
        for (i in 7uL downTo 1uL step 0L) {
        }
    }

    return "OK"
}