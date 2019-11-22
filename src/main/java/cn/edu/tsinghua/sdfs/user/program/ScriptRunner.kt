package cn.edu.tsinghua.sdfs.user.program

import cn.edu.tsinghua.sdfs.server.mapreduce.JobContext
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import javax.script.ScriptContext
import javax.script.ScriptEngineManager

object ScriptRunner {
    private const val INIT_SCRIPT = """
        fun sdfsMap(mapFunc: Any) {
            (bindings["functions"] as MutableList<Pair<String, (Any) -> Any>>).apply {
                add(Pair("map", mapFunc as ((Any) -> Any)))
            }
        }

        fun sdfsShuffle(shuffleFunc: Any) {
            (bindings["functions"] as MutableList<Pair<String, (Any) -> Any>>).apply {
                add(Pair("shuffle", shuffleFunc as ((Any) -> Any)))
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

    init {
        setIdeaIoUseFallback()
    }

    // todo: cache user program?
    fun compile(program: String): JobContext {
        val engine = ScriptEngineManager().getEngineByExtension("kts")
        val functions = mutableListOf<Pair<String, (Any) -> Any>>()

        val file = StringBuilder()
        // (file as StringBuilder).append()
        val bindings = engine.createBindings().apply {
            put("functions", functions)
            put("file", file)
        }
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE)
        engine.eval(INIT_SCRIPT)

        engine.eval(program, bindings)

        System.gc()

        return JobContext(file.toString(), functions, -1)
    }
}
