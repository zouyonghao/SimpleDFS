package cn.edu.tsinghua.sdfs.user.program

import cn.edu.tsinghua.sdfs.server.mapreduce.JobContext
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngine
import javax.script.ScriptContext
import javax.script.ScriptEngineManager

object ScriptRunner {
    val engine = ScriptEngineManager().getEngineByExtension("kts")!! as KotlinJsr223JvmLocalScriptEngine

    init {
        setIdeaIoUseFallback()
        engine.eval("")
    }

    const val INIT_SCRIPT = """
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
        
        fun sdfsRead(file: String) {
            (bindings["file"] as StringBuilder).append(file)
        }
    """

    fun compile(program: String): JobContext {
        val functions = mutableListOf<Pair<String, (Any) -> Any>>()

        val file = StringBuilder()
        // (file as StringBuilder).append()
        engine.getBindings(ScriptContext.ENGINE_SCOPE).apply {
            put("functions", functions)
            put("file", file)
        }

        engine.eval(INIT_SCRIPT)

        engine.eval(program)

        return JobContext(file.toString(), functions, -1)
    }
}
