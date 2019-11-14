package cn.edu.tsinghua.sdfs.server

import cn.edu.tsinghua.sdfs.config
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Paths

object DataManager {

    private val ROOT_DIR = Paths.get(config.slaves[0].folder)

    init {
        if (Files.exists(ROOT_DIR)) {
            Files.createDirectories(ROOT_DIR)
        }
    }

    fun getFileChannel(filePath:String): RandomAccessFile {
        val localPath = Paths.get(ROOT_DIR.toString(), filePath)

        return RandomAccessFile(localPath.toFile(), "rw")
    }
}