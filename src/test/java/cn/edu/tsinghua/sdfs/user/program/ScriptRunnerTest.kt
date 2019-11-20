package cn.edu.tsinghua.sdfs.user.program

import org.junit.jupiter.api.Test
import javax.script.Invocable
import javax.script.ScriptContext

internal class ScriptRunnerTest {

    @Test
    fun engineTest() {
        ScriptRunner.engine.eval("println(\"hello world\")")

        ScriptRunner.engine.eval("""
fun reduce(list: List<Int>) = list.reduce(operation = { a, b -> a + b })
""")
        val invocable = ScriptRunner.engine as? Invocable

        val data = listOf(1, 2, 3, 4)
        val res = (invocable!!.invokeFunction("reduce", data)
                ?: run {
                    println("reduce function not exist!")
                    return@engineTest
                })
        println(res)

        println(ScriptRunner.engine.eval("""
        "abc/number"
"""))

        ScriptRunner.engine.getBindings(ScriptContext.ENGINE_SCOPE).apply {
            put("file", "1\n2\n3")
        }

        val functions = mutableListOf<Pair<String, (Any) -> Any>>()

        val file = StringBuilder()
        // (file as StringBuilder).append()
        ScriptRunner.engine.getBindings(ScriptContext.ENGINE_SCOPE).apply {
            put("functions", functions)
            put("file", file)
        }

        ScriptRunner.engine.eval("""
fun sdfsMap(mapFunc: Any) {
    (bindings["functions"] as MutableList<Pair<String, (Any) -> Any>>).apply {
        add(Pair("map", mapFunc as ((Any) -> Any)))
    }
}

fun sdfsReduce(reduceFunc: Any) {
    (bindings["functions"] as MutableList<Pair<String, (Any) -> Any>>).apply {
        add(Pair("reduce", reduceFunc as ((Any) -> Any)))
    }
}

fun sdfsShuffle(shuffleFunc: Any) {
    (bindings["functions"] as MutableList<Pair<String, (Any) -> Any>>).apply {
        add(Pair("shuffle", shuffleFunc as ((Any) -> Any)))
    }
}

fun sdfsRead(file: String) {
    (bindings["file"] as StringBuilder).append(file)
}
    """)

        ScriptRunner.engine.eval(
                """
                sdfsRead("1\n2\n444\n555\n100001")
                sdfsMap({ a: String -> a.split("\n") })
                sdfsMap({ a: List<String> -> a.map{ it.toInt() } })
                sdfsShuffle {a:Int -> a % 10000}
                sdfsReduce({ a: List<Int> -> a.reduce { i, j -> i + j } })
"""
        )

        var lastResult = file.toString() as Any
        functions.forEach {
            println(it.first)
            if (it.first == "shuffle") {
                val function = it.second as (Any) -> Int
                (lastResult as List<Any>).forEach {
                    println(function.invoke(it))
                }
            } else {
                lastResult = it.second(lastResult)
                println(lastResult)
            }
        }

    }
}