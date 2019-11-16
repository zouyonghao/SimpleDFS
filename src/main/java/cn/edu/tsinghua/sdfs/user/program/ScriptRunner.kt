package cn.edu.tsinghua.sdfs.user.program

import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngine
import javax.script.Invocable
import javax.script.ScriptContext
import javax.script.ScriptEngineManager

fun main() {
    setIdeaIoUseFallback()
    val engine = ScriptEngineManager().getEngineByExtension("kts")!! as KotlinJsr223JvmLocalScriptEngine
    engine.eval("println(\"hello world\")")

    engine.eval("""
fun reduce(list: List<Int>) = list.reduce(operation = { a, b -> a + b })
""")
    val invocable = engine as? Invocable

    val data = listOf(1, 2, 3, 4)
    val res = (invocable!!.invokeFunction("reduce", data)
            ?: run {
                println("reduce function not exist!")
                return@main
            })
    println(res)

    println(engine.eval("""
        "abc/number"
"""))

    engine.getBindings(ScriptContext.ENGINE_SCOPE).apply {
        put("file", "1\n2\n3")
    }

    val functions = mutableListOf<Pair<String, (Any) -> Any>>()
    val splitFile = { a: String -> a.split("\n") }
    functions.apply {
        // add(Pair("map", splitFile as (Any) -> Any))
    }
    engine.getBindings(ScriptContext.ENGINE_SCOPE).apply {
        put("functions", functions)
    }


    val mapFunc = engine.eval(
"""
val splitFile = { a: String -> a.split("\n") }
val mapFunc = { a: List<String> -> a.map{ it.toInt() } }
val reduce = { a: List<Int> -> a.reduce { i, j -> i + j } }
(bindings["functions"] as MutableList<Pair<String, (Any) -> Any>>).apply {
    add(Pair("map", splitFile as ((Any) -> Any)))
    add(Pair("map", mapFunc as ((Any) -> Any)))
    add(Pair("reduce", reduce as ((Any) -> Any)))
}
mapFunc
"""
    ) as ((List<String>) -> List<Int>)

    println(mapFunc(listOf("1", "23124")))

    var lastResult: Any = "1\n2\n444\n666"
    functions.forEach {
        println(it.first)
        lastResult = it.second(lastResult)
        println(lastResult)
    }

}
