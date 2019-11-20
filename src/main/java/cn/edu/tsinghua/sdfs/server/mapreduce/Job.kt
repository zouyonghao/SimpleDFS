package cn.edu.tsinghua.sdfs.server.mapreduce

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

data class JobContext(val file: String,
                      @JSONField(serialize = false)
                      val functions: MutableList<Pair<String, (Any) -> Any>>?,
                      var currentPc: Int)

data class Job(val id: String = UUID.randomUUID().toString(),
               val userProgram: UserProgram,
               var status: JobStatus) {
    lateinit var jobContext: JobContext
}