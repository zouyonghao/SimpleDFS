package cn.edu.tsinghua.sdfs.server.mapreduce

import cn.edu.tsinghua.sdfs.protocol.packet.impl.mapreduce.DoReducePacket
import cn.edu.tsinghua.sdfs.server.slave.slave
import cn.edu.tsinghua.sdfs.user.program.ScriptRunner
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.CountDownLatch

object Reducer {

    private val REDUCER_DIR = Paths.get(slave.folder, "__reduce__")

    init {
        if (Files.notExists(REDUCER_DIR)) {
            Files.createDirectories(REDUCER_DIR)
        }
    }

    fun doReduce(packet: DoReducePacket) {

        // 1. retrieve intermediate file from other slaves

        val countDownLatch = CountDownLatch(packet.intermediateFiles.size)

        println("downloading intermediate files...")
        val reducePartitions = mutableListOf<String>()
        packet.intermediateFiles.forEachIndexed { index, intermediateFile ->
            println(intermediateFile)
            val fileDownloader = IntermediateFileDownloader()
            fileDownloader.countDownLatch = countDownLatch

            // data_sdfs/__reducer__/jobId/partition
            fileDownloader.localFile = Paths.get(slave.folder,
                    intermediateFile.file.replace("__map__", "__reduce__"), index.toString()).toString()

            reducePartitions.add(fileDownloader.localFile)
            Files.createDirectories(Paths.get(fileDownloader.localFile).parent)
            fileDownloader.download(intermediateFile)
        }
        countDownLatch.await()

        // FileUtil.mergeFiles()

        // 2. run reduce func

        val job = packet.job
        val currentPc = job.jobContext.currentPc
        job.jobContext = ScriptRunner.compile(job.userProgram.content)

        val reduceResultFiles = mutableSetOf<IntermediateFile>()

        job.jobContext.currentPc = currentPc
        val pair = job.jobContext.functions!![currentPc]
        val type = pair.first
        assert(type == "reduce")
        // todo: sort
        reducePartitions.forEach { partition ->
            var lastResult = String(Files.readAllBytes(Paths.get(partition))).split("\n") as Any
            val reduceParamType = pair.second.toString()
                    .substringBefore(") ->")
                    .substringAfter("(")
            when (reduceParamType) {
                "kotlin.collections.List<kotlin.Int>" -> {
                    println("reduce func's parameter is List<Int>")
                    lastResult = (lastResult as List<String>).filter { it.isNotEmpty() }.map { line -> line.toInt() }
                    lastResult = pair.second(lastResult)

                    // each intermediate file have a result file, which should be merged
                    val path = Paths.get("$partition.result")
                    Files.write(path, lastResult.toString().toByteArray())
                    val relativePath = path.subpath(1, path.nameCount)
                    reduceResultFiles.add(IntermediateFile(packet.server, relativePath.toString()))
                }
                else -> {
                    println("Unsupport type yet $reduceParamType")
                }
            }

            System.gc()
        }

        job.jobContext.reduceResultFiles[packet.filePartition] = reduceResultFiles
        job.jobContext.currentPc++
    }

    fun getResult(file: String): String {
        return String(Files.readAllBytes(Paths.get(slave.folder, file)))
    }
}