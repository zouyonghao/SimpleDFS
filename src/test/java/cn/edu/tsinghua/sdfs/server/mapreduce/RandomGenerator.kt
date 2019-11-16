package cn.edu.tsinghua.sdfs.server.mapreduce

import org.junit.jupiter.api.Test
import java.io.RandomAccessFile
import kotlin.random.Random

internal class RandomGenerator {

    @Test
    fun generateNumToTestFile() {
        val f = RandomAccessFile("test_file/number", "rw")
        val r = Random(100000L)
        for (i in 0 until 1000000) {
            f.writeBytes("${r.nextInt()}\n")
        }
    }

}