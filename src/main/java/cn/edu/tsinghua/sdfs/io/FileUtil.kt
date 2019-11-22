package cn.edu.tsinghua.sdfs.io

import java.io.IOException
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.ArrayList


object FileUtil {
    /**
     * Split a file into multiples files.
     * TODO: this method may cut a line
     *
     * @param fileName      Name of file to be split.
     * @param splitSize     Maximum number of MB per file.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun splitFile(fileName: String, splitSize: Long): List<Path> {

        require(splitSize > 0) { "splitSize must be more than zero" }

        val partFiles = ArrayList<Path>()
        val sourceSize = Files.size(Paths.get(fileName))
        val bytesPerSplit = 1024L * 1024L * splitSize
        val numSplits = sourceSize / bytesPerSplit
        val remainingBytes = sourceSize % bytesPerSplit
        var position = 0

        with(RandomAccessFile(fileName, "r")) {
            while (position < numSplits) {
                //write multipart files.
                writePartToFile(bytesPerSplit, position * bytesPerSplit, channel, partFiles, fileName)
                position++
            }
            if (remainingBytes > 0) {
                writePartToFile(remainingBytes, position * bytesPerSplit, channel, partFiles, fileName)
            }

            close()
        }
        return partFiles
    }

    @Throws(IOException::class)
    private fun writePartToFile(byteSize: Long, position: Long, sourceChannel: FileChannel, partFiles: MutableList<Path>, fileName: String) {
        val file = Paths.get(fileName + String.format("%07d", partFiles.size))

        // TODO: transfer file when split
        with(RandomAccessFile(file.toFile(), "rw")) {
            sourceChannel.position(position)
            channel.transferFrom(sourceChannel, 0, byteSize)

            close()
        }

        partFiles.add(file)
    }

    fun mergeFiles(filePattern: String, output: String) {
        val path = Paths.get(filePattern)
        val mergedFile = RandomAccessFile(output, "rw")
        var index = 0L
        Files.walk(path.parent ?: Paths.get("."), 1)
                .sorted()
                .filter { it.fileName.toString().contains(Regex("${path.fileName}\\d{7}")) }

                .forEachOrdered {
                    println(it.fileName)
                    RandomAccessFile(it.toFile(), "r").let { part ->
                        mergedFile.channel.transferFrom(
                                part.channel,
                                index,
                                index + part.length()
                        )
                        index += part.length()
                        part.close()
                    }
                    Files.deleteIfExists(it)
                }
        mergedFile.close()
    }


}