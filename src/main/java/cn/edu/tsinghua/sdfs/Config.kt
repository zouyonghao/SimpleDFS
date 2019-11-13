package cn.edu.tsinghua.sdfs

import com.alibaba.fastjson.JSON
import java.nio.file.Files
import java.nio.file.Paths

data class Server(val ip: String, val port: Int)

data class Config(
        val master: Server,
        val slaves: List<Server>,
        val nameFolder: String,
        val dataFolder: String,
        val dfsReplication: Int,
        val dfsBlkSize: Int
)

val config: Config = JSON.parseObject(Files.readAllBytes(Paths.get("config.json")), Config::class.java)