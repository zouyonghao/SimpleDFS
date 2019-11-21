package cn.edu.tsinghua.sdfs.server.slave

import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object DataManager {

    val ROOT_DIR = Paths.get(slave.folder)

    init {
        if (Files.notExists(ROOT_DIR)) {
            Files.createDirectories(ROOT_DIR)
        }
    }

    fun getFile(filePath: String): RandomAccessFile {
        val localPath = Paths.get(ROOT_DIR.toString(), filePath)
        return RandomAccessFile(localPath.toFile(), "rw")
    }

    fun deleteAndCreate(filePath: String): RandomAccessFile {
        val localPath = Paths.get(ROOT_DIR.toString(), filePath)
        deleteFile(filePath)
        return RandomAccessFile(localPath.toFile(), "rw")
    }

    fun deleteFile(filePath: String) {
        val localPath = Paths.get(ROOT_DIR.toString(), filePath)
        val dir = localPath.parent
        if (Files.exists(dir) && !Files.isDirectory(dir)) {
            Files.delete(dir)
        }
        Files.createDirectories(dir)

        Files.deleteIfExists(localPath)
    }

    fun getFilePath(file: String, partition: Int): Path {
        return Paths.get(ROOT_DIR.toString(), file + String.format("%07d", partition))
    }

    fun getFileAsString(localFilePath: Path): String {
        return String(Files.readAllBytes(localFilePath))
    }
}