package cn.edu.tsinghua.sdfs.server.mapreduce

import cn.edu.tsinghua.sdfs.protocol.packet.impl.UserProgram
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object UserProgramManager {
    lateinit var ROOT_DIR: Path

    private const val userProgramDir = "__program__"

    fun saveUserProgram(userProgram: UserProgram) {
        val savePath = Paths.get(
                ROOT_DIR.toString(),
                userProgramDir)
        if (Files.notExists(savePath)) {
            Files.createDirectories(savePath)
        }

        Files.write(Paths.get(savePath.toString(), userProgram.id), userProgram.content.toByteArray())

    }
}