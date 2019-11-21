package cn.edu.tsinghua.sdfs.server.mapreduce

import cn.edu.tsinghua.sdfs.protocol.packet.impl.mapreduce.DoMapPacket
import cn.edu.tsinghua.sdfs.server.slave.DataManager
import cn.edu.tsinghua.sdfs.server.slave.slave
import cn.edu.tsinghua.sdfs.user.program.ScriptRunner
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object Mapper {

    private val MAPPER_DIR = Paths.get(slave.folder, "__map__")

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
            val intermediateFiles = mutableMapOf<Int, RandomAccessFile>()
            when (type) {
                "map" -> {
                    println(lastResult.javaClass)
                    lastResult = function(lastResult)
                    currentPc++
                }
                "shuffle" -> {
                    (lastResult as List<*>).forEach {
                        val reducePartition = function(it!!) as Int
                        val intermediateFilePath = getIntermediateFile(job, reducePartition)
                        if (!intermediateFiles.containsKey(reducePartition)) {
                            intermediateFiles.put(reducePartition,
                                    RandomAccessFile(intermediateFilePath.toFile(), "rw"))
                        }
                        intermediateFiles[reducePartition]!!.apply {
                            // println(it.javaClass)
                            write(it.toString().toByteArray())
                            write("\n".toByteArray())
                        }
                        if (!job.jobContext.mapIntermediateFiles.containsKey(reducePartition)) {
                            job.jobContext.mapIntermediateFiles.put(reducePartition, mutableSetOf())
                        }
                        job.jobContext.mapIntermediateFiles[reducePartition]!!.add(
                                IntermediateFile(packet.slave,
                                        intermediateFilePath.subpath(1, intermediateFilePath.nameCount).toString()))
                    }
                    break@loop
                }
                else -> {
                    break@loop
                }
            }

            System.gc()
        }

        job.jobContext.currentPc = currentPc
    }

    private fun getIntermediateFile(job: Job, reducePartition: Int): Path {
        val jobDir = Paths.get(MAPPER_DIR.toString(), job.id)
        if (Files.notExists(jobDir)) {
            Files.createDirectories(jobDir)
        }
        return Paths.get(jobDir.toString(), String.format("%07d", reducePartition))
    }
}