package cn.edu.tsinghua.sdfs.protocol.serilizer


import cn.edu.tsinghua.sdfs.protocol.serilizer.impl.JSONSerializer

interface Serializer {

    fun serialize(obj: Any): ByteArray

    fun <T> deserialize(bytes: ByteArray, clazz: Class<out T>): T

    companion object {

        val DEFAULT: Serializer = JSONSerializer()
    }

}
