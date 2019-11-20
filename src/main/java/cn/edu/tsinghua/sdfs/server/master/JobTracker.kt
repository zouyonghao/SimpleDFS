package cn.edu.tsinghua.sdfs.server.master

import cn.edu.tsinghua.sdfs.protocol.packet.impl.UserProgram
import cn.edu.tsinghua.sdfs.protocol.packet.impl.mapreduce.DoMapPacket
import cn.edu.tsinghua.sdfs.server.mapreduce.Job
import cn.edu.tsinghua.sdfs.server.mapreduce.JobStatus.FAIL
import cn.edu.tsinghua.sdfs.server.mapreduce.JobStatus.FINISHED
import cn.edu.tsinghua.sdfs.server.mapreduce.JobStatus.INIT
import cn.edu.tsinghua.sdfs.server.mapreduce.JobStatus.RUNNING
import cn.edu.tsinghua.sdfs.server.mapreduce.JobStatus.SUSPEND
import cn.edu.tsinghua.sdfs.user.program.ScriptRunner
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object JobTracker {
    lateinit var ROOT_DIR: Path

    // TODO: save job state to disk
    private const val userProgramDir = "__job__"

    val jobMap = mutableMapOf<String, Job>()

    private val jobExecutor = Executors.newScheduledThreadPool(1)

    init {
        jobExecutor.scheduleAtFixedRate({
            val jobNeedRemove = mutableListOf<Job>()
            jobMap.values.forEach {
                when (it.status) {
                    INIT -> it.status = RUNNING
                    RUNNING -> {
                        if (it.jobContext.currentPc >= it.jobContext.functions!!.size) {
                            it.status = FINISHED
                            return@forEach
                        }
                        it.jobContext.currentPc++
                        runCurrentFunc(it)
                    }
                    FAIL -> {
                        println("Job $it failed.")
                        jobNeedRemove.add(it)
                    }
                    FINISHED -> jobNeedRemove.add(it)
                    SUSPEND -> println("job still suspend")
                }
            }
            jobMap.values.removeAll(jobNeedRemove)
        }, 0, 3, TimeUnit.SECONDS)
    }

    fun startJob(userProgram: UserProgram) {
        val job = Job(userProgram.id, userProgram, INIT)
        job.jobContext = ScriptRunner.compile(userProgram.content)
        submitJob(job)
    }

    private fun submitJob(job: Job) {
        if (job.jobContext.file.isEmpty()) {
            println("No file specified in script ${job.userProgram.id}, exit")
            job.status = FAIL
            return
        }
        if (!NameManager.getNameItem(job.jobContext.file).exist) {
            println("File specified in script ${job.userProgram.id} not exist, exit")
            job.status = FAIL
            return
        }
        // job executed in jobExecutor
        jobMap.putIfAbsent(job.id, job)
    }

    private fun runCurrentFunc(job: Job) {
        val pair = job.jobContext.functions!![job.jobContext.currentPc]
        when (pair.first) {
            "map" -> {
                val nameItem = NameManager.getNameItem(job.jobContext.file)
                nameItem.partitions.forEachIndexed { index, slaves ->
                    val slave = SlaveManager.doMap(job, slaves, index) ?: TODO("retry")
                    println("running a map job with index $index on $slave")
                }
            }
        }
        job.status = SUSPEND
    }

    fun mapFinished(packet: DoMapPacket) {
        jobMap[packet.job.id]?.apply {
            packet.job.jobContext.mapIntermediateFiles.forEach{reducePartition, intermediateFiles ->
                run {
                    jobContext.mapIntermediateFiles.putIfAbsent(reducePartition, intermediateFiles)
                    jobContext.mapIntermediateFiles[reducePartition]!!.addAll(intermediateFiles)
                }
            }

            println("partition ${packet.partition}")
            if (NameManager.getNameItem(jobContext.file).partitions.size == jobContext.mapIntermediateFiles.size) {
                println("all partition map finished!")
            }
        }
    }
}