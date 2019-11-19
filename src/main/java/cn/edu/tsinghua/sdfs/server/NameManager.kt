package cn.edu.tsinghua.sdfs.server

import cn.edu.tsinghua.sdfs.Server
import cn.edu.tsinghua.sdfs.config
import cn.edu.tsinghua.sdfs.protocol.packet.impl.NameItem
import cn.edu.tsinghua.sdfs.protocol.serilizer.impl.JSONSerializer
import com.alibaba.fastjson.JSON
import java.nio.file.Files
import java.nio.file.Paths

fun NameItem.toJsonString() = JSON.toJSONString(this)!!

object NameManager {
    private val ROOT_DIR = Paths.get(config.master.folder)

    init {
        if (Files.notExists(ROOT_DIR)) {
            Files.createDirectories(ROOT_DIR)
        }
    }

    fun getNameItemForDownload(filePath: String): NameItem {
        val dir = Paths.get(ROOT_DIR.toString(), filePath)
        val item = Paths.get(dir.toAbsolutePath().toString(), "item.json")

        return if (Files.exists(item)) {
            JSONSerializer().deserialize(Files.readAllBytes(item), NameItem::class.java).apply {
                exist = true
                download = true
            }
        } else {
            NameItem(0, emptyList<MutableList<Server>>().toMutableList(), 0).apply {
                exist = false
            }
        }
    }

    fun createOrGet(filePath: String, fileSize: Long): NameItem {
        val dir = Paths.get(ROOT_DIR.toString(), filePath)
        val item = Paths.get(dir.toAbsolutePath().toString(), "item.json")

        if (Files.exists(item)) {
            val nameItem = JSONSerializer().deserialize(Files.readAllBytes(item), NameItem::class.java)
            nameItem.exist = true
            // TODO: user can choose whether to delete this file
            Files.delete(item)
            return nameItem
        }

        if (Files.notExists(dir)) {
            Files.createDirectories(dir)
        }

        val bytesPerSplit = 1024L * 1024L * config.blockSize
        val numSplits = fileSize / bytesPerSplit
        val remainingBytes = fileSize % bytesPerSplit

        val nameItem = NameItem(fileSize, mutableListOf(), config.blockSize)


        for (i in 0 until numSplits) {
            allocateSlave(nameItem)
        }
        if (remainingBytes > 0) {
            allocateSlave(nameItem)
        }
        Files.write(item, nameItem.toJsonString().toByteArray())
        return nameItem
    }

    private fun allocateSlave(nameItem: NameItem) {
        val list = mutableListOf<Server>()
        nameItem.partitions.add(list)
        for (j in 0 until config.replication) {
            list.add(config.slaves.random())
        }
    }

    fun ls(filePath: String): String {
        val dir = Paths.get(ROOT_DIR.toString(), filePath)
        return Files.walk(dir, 1).map { it.subpath(1, it.nameCount) }.toArray().joinToString("\n")
    }
}