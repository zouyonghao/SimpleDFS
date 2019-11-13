package cn.edu.tsinghua.sdfs.user.program

import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
import javax.script.Invocable

fun main() {
    val engine = KotlinJsr223JvmLocalScriptEngineFactory().scriptEngine
    engine.eval("println(\"hello world\")")
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

}
