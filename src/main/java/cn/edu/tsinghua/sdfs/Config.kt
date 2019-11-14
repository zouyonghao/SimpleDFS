package cn.edu.tsinghua.sdfs

import com.alibaba.fastjson.JSON
import java.nio.file.Files
import java.nio.file.Paths

data class Server(val ip: String, val port: Int, val folder: String)

data class Config(
        val master: Server,
        val slaves: List<Server>,
        val replication: Int,
        val blockSize: Long
) {
    init {
        if (slaves.size < replication) {
            error("Replication should not large than slaves!")
        }
    }
}

val config: Config = JSON.parseObject(Files.readAllBytes(Paths.get("config.json")), Config::class.java)