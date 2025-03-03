// The local variables `y` and `i` are visible in finally blocks with old backend.
// IGNORE_BACKEND: JVM
// WITH_RUNTIME
// FILE: test.kt

fun box(): String {
    try {
        for (i in 0 until 1) {
            try {
                val x = "x"
                throw RuntimeException(x)
            } catch (e: Exception) {
                val y = "y"
                return "FAIL1"
            } finally {
                return "FAIL2"
            }
        }
    } finally {
        return "OK"
    }
    return "FAIL3"
}

// EXPECTATIONS
// test.kt:7 box:
// test.kt:8 box:
// test.kt:9 box: i:int=0:int
// test.kt:10 box: i:int=0:int
// test.kt:11 box: i:int=0:int, x:java.lang.String="x":java.lang.String
// test.kt:12 box: i:int=0:int
// test.kt:13 box: i:int=0:int, e:java.lang.Exception=java.lang.RuntimeException
// test.kt:14 box: i:int=0:int, e:java.lang.Exception=java.lang.RuntimeException, y:java.lang.String="y":java.lang.String
// test.kt:16 box: i:int=0:int
// test.kt:20 box:
