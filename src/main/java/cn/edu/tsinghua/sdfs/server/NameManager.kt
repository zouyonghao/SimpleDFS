package cn.edu.tsinghua.sdfs.server

import cn.edu.tsinghua.sdfs.config
import com.alibaba.fastjson.JSON
import java.nio.file.Files
import java.nio.file.Paths

/**
 * {
 *  "fileSize": 1000 (bytes)
 *  "partitions": [
 *      [1, 3, 4], // partition1 in slave1,3,4
 *      [2, 4, 5]
 *  ]
 * }
 */
data class NameItem(
        val fileSize: Long,
        val partitions: List<List<Int>>
)

fun NameItem.toJsonString() = JSON.toJSONString(this)!!

object NameManager {
    val ROOT_DIR = Paths.get(config.nameFolder)

    init {
        if (Files.exists(ROOT_DIR)) {
            Files.createDirectories(ROOT_DIR)
        }
    }

    fun create(filePath: String, fileSize: Long): NameItem {
        val dir = Paths.get(ROOT_DIR.toString(), filePath)
        val item = Paths.get(dir.toAbsolutePath().toString(), "item.json")

        if (Files.notExists(dir)) {
            Files.createDirectories(dir)
        }
        val nameItem = NameItem(fileSize, emptyList())
        Files.write(item, nameItem.toJsonString().toByteArray())
        return nameItem
    }
}