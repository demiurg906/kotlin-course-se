var x = 10

fun foo(x) {
    var y = x + x * x + x
    if (x % 2 == 0) {
        return y * 2
    }
    return y
}

println(foo(10))
println(foo(1))
