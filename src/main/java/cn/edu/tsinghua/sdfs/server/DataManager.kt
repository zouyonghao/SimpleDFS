package cn.edu.tsinghua.sdfs.server

import cn.edu.tsinghua.sdfs.config
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Paths

object DataManager {

    private val ROOT_DIR = Paths.get(config.slaves[0].folder)

    init {
        if (Files.notExists(ROOT_DIR)) {
            Files.createDirectories(ROOT_DIR)
        }
    }

    fun getRandomAccessFile(filePath: String): RandomAccessFile {
        val localPath = Paths.get(ROOT_DIR.toString(), filePath)
        val dir = localPath.parent
        if (Files.exists(dir) && !Files.isDirectory(dir)) {
            Files.delete(dir)
        }
        Files.createDirectories(dir)

        Files.deleteIfExists(localPath)

        return RandomAccessFile(localPath.toFile(), "rw")
    }
}