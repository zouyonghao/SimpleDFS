package cn.edu.tsinghua.sdfs.user.program

import org.junit.jupiter.api.Test

class Test {

    @Test
    fun test() {
        // val reduce = "1000\n1\n24124\n435".split("\n").map { it.toDouble() * it.toDouble() }.reduce { i, j -> i + j }
        // println(reduce)
        println("2745434".toDouble())
        println("1.09852134E8".toDouble().toInt())
    }

    @Test
    fun mean() {
        val sum = "1.499665229529E12".toDouble()
        val count = "3.00000005E8".toDouble()
        val sum_square = "9.996289592603564E15".toDouble()
        println("count = $count")
        println("sum = $sum")
        println("sum_square = $sum_square")

        println()

        val mean = sum / count
        println("mean = $mean")

        val var_x = sum_square / count - mean * mean
        println("var = ${var_x}")
    }

}