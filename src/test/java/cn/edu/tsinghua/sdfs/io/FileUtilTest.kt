package cn.edu.tsinghua.sdfs.io

import org.jetbrains.kotlin.incremental.md5
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.streams.toList
import kotlin.test.assertEquals

@Execution(ExecutionMode.SAME_THREAD)
@Disabled
internal class FileUtilTest {

    companion object {
        const val TEST_FILE = "test_file/test.jar"
        const val TEST_FILE_MERGE = "test_file/test.jar.bak"
        @BeforeAll
        @AfterAll
        @JvmStatic
        fun clean() {
            Files.walk(Paths.get("test_file"))
                    .filter { it.toString().contains(Regex("(jar[0-9]{7})|(bak[0-9]{7})|output")) }
                    .toList()
                    .forEach {
                        println(it)
                        Files.deleteIfExists(it)
                    }
        }
    }

    @Test
    fun splitFile() {
        FileUtil.splitFile(TEST_FILE, 10)
        val count = Files.walk(Paths.get("test_file")).filter { it.toString().contains(Regex("jar[0-9]{7}")) }.count()
        assertEquals(6, count)
    }

    @Test
    fun mergeFile() {
        Files.deleteIfExists(Paths.get(TEST_FILE_MERGE))
        Files.copy(Paths.get(TEST_FILE), Paths.get(TEST_FILE_MERGE))

        val md5 = Files.readAllBytes(Paths.get(TEST_FILE_MERGE)).md5()
        FileUtil.splitFile(TEST_FILE_MERGE, 10)

        FileUtil.mergeFiles(TEST_FILE_MERGE, TEST_FILE_MERGE)
        assertEquals(md5, Files.readAllBytes(Paths.get(TEST_FILE_MERGE)).md5())

        Files.deleteIfExists(Paths.get(TEST_FILE_MERGE))
    }

    @Test
    @Disabled
    fun testNumber() {
        // FileUtil.splitFile("test_file/numberFile", 100)
    }
}