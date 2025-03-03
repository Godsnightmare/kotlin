// WITH_RUNTIME

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE")
@kotlin.jvm.JvmInline
value class X(val x: String?)

fun useX(x: X): String = x.x ?: "fail: $x"

fun <T> call(fn: () -> T) = fn()

fun box() = useX(call { X("OK") })