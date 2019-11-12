package cn.edu.tsinghua.sdfs.user.program

import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory

fun main() {
    val factory = KotlinJsr223JvmLocalScriptEngineFactory().scriptEngine
    factory.eval("println(\"hello world\")")
    factory.eval("println(\"hello world\")")
}