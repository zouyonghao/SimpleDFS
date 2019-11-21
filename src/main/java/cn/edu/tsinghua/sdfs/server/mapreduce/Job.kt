package cn.edu.tsinghua.sdfs.server.mapreduce

import cn.edu.tsinghua.sdfs.Server
import cn.edu.tsinghua.sdfs.protocol.packet.impl.UserProgram
import com.alibaba.fastjson.annotation.JSONField
import java.util.UUID

enum class JobStatus {
    INIT,
    RUNNING,
    SUSPEND,
    FAIL,
    FINISHED
}

data class IntermediateFile(val server: Server, val file: String)

data class JobContext(val file: String,
                      @JSONField(serialize = false)
                      val functions: MutableList<Pair<String, (Any) -> Any>>?,
                      var currentPc: Int) {
    val mapper = mutableListOf<Server>()
    val finishedMapper = mutableListOf<Server>()
    val reducer = mutableListOf<Server>()
    val finishedReducer = mutableListOf<Server>()
    // reduced partition to Map result
    var mapIntermediateFiles = mutableMapOf<Int, MutableSet<IntermediateFile>>()
    var reduceResultFiles = mutableMapOf<Int, MutableSet<IntermediateFile>>()
}

data class Job(val id: String = UUID.randomUUID().toString(),
               val userProgram: UserProgram,
               var status: JobStatus) {
    lateinit var jobContext: JobContext
}