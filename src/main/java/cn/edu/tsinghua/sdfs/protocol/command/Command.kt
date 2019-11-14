package cn.edu.tsinghua.sdfs.protocol.command

interface Command {
    companion object {
        const val CREATE_REQUEST = 1
        const val LS = 2
        // do not support yet
        // const val CD = 3
        // const val PWD = 4
        const val COPY_FROM_LOCAL = 5
        const val COPY_TO_LOCAL = 6
        const val RM = 7

        const val FILE_PACKET = 8

        const val RESULT = 10
        const val NAME_ITEM = 11
    }
}
