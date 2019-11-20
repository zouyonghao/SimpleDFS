package cn.edu.tsinghua.sdfs.server.mapreduce

import cn.edu.tsinghua.sdfs.protocol.packet.impl.slave.DoMapPacket
import cn.edu.tsinghua.sdfs.server.slave.DataManager
import cn.edu.tsinghua.sdfs.server.slave.slave
import cn.edu.tsinghua.sdfs.user.program.ScriptRunner
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Paths

object Mapper {

    val MAPPER_DIR = Paths.get(slave.folder, "__map__")

    init {
        if (Files.notExists(MAPPER_DIR)) {
            Files.createDirectories(MAPPER_DIR)
        }
    }

    fun doMap(packet: DoMapPacket) {
        val job = packet.job
        var currentPc = job.jobContext.currentPc
        job.jobContext = ScriptRunner.compile(job.userProgram.content)

        val localFilePath = DataManager.getFilePath(job.jobContext.file, packet.partition)
        var lastResult = DataManager.getFileAsString(localFilePath) as Any
        val functions = job.jobContext.functions!!
        loop@ for (i in currentPc until functions.size) {
            val type = functions[i].first
            val function = functions[i].second
            val intermediateFiles = mutableListOf<RandomAccessFile>()
            when (type) {
                "map" -> {
                    println(lastResult.javaClass)
                    lastResult = function(lastResult)
                    currentPc++
                }
                "shuffle" -> {
                    (lastResult as List<*>).forEach {
                        val reducePartition = function(it!!) as Int
                        if (reducePartition >= intermediateFiles.size) {
                            intermediateFiles.add(getIntermediateFile(job, reducePartition))
                        }
                        intermediateFiles[reducePartition].apply {
                            write(it.toString().toByteArray())
                            write("\n".toByteArray())
                        }

                    }
                    currentPc++
                }
                else -> {
                    break@loop
                }
            }
        }

        job.jobContext.currentPc = currentPc - 1

    }

    fun getIntermediateFile(job: Job, reducePartition: Int): RandomAccessFile {
        val jobDir = Paths.get(MAPPER_DIR.toString(), job.id)
        if (Files.notExists(jobDir)) {
            Files.createDirectories(jobDir)
        }
        val reducePartitionFile = Paths.get(jobDir.toString(), reducePartition.toString())
        return RandomAccessFile(reducePartitionFile.toString(), "rw")
    }
}