package cn.edu.tsinghua.sdfs.io

import org.jetbrains.kotlin.incremental.md5
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.streams.toList
import kotlin.test.assertEquals

@Execution(ExecutionMode.SAME_THREAD)
internal class FileUtilsTest {

    companion object {
        const val TEST_FILE = "test_file/SimpleDFS-1.0-SNAPSHOT-jar-with-dependencies.jar"
        const val TEST_FILE_MERGE = "test_file/SimpleDFS-1.0-SNAPSHOT-jar-with-dependencies.jar.bak"
        @BeforeAll
        @AfterAll
        @JvmStatic
        fun clean() {
            Files.walk(Paths.get("test_file"))
                    .filter { it.toString().contains(Regex("(jar[0-9])|(bak[0-9])|output")) }
                    .toList()
                    .forEach {
                        // println(it)
                        Files.deleteIfExists(it)
                    }
        }
    }

    @Test
    fun splitFile() {
        FileUtils.splitFile(TEST_FILE, 10)
        val count = Files.walk(Paths.get("test_file")).filter { it.toString().contains(Regex("jar[0-9]")) }.count()
        assertEquals(6, count)
    }

    @Test
    fun mergeFile() {
        Files.deleteIfExists(Paths.get(TEST_FILE_MERGE))
        Files.copy(Paths.get(TEST_FILE), Paths.get(TEST_FILE_MERGE))

        val md5 = Files.readAllBytes(Paths.get(TEST_FILE_MERGE)).md5()
        FileUtils.splitFile(TEST_FILE_MERGE, 10)

        FileUtils.mergeFiles(TEST_FILE_MERGE, TEST_FILE_MERGE)
        assertEquals(md5, Files.readAllBytes(Paths.get(TEST_FILE_MERGE)).md5())

        Files.deleteIfExists(Paths.get(TEST_FILE_MERGE))
    }
}