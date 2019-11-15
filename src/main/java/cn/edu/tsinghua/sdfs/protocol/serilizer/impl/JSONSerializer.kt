package cn.edu.tsinghua.sdfs.protocol.serilizer.impl


import cn.edu.tsinghua.sdfs.protocol.serilizer.Serializer
import com.alibaba.fastjson.JSON

class JSONSerializer : Serializer {

    override fun serialize(obj: Any): ByteArray {
        return JSON.toJSONBytes(obj)
    }

    override fun <T> deserialize(bytes: ByteArray, clazz: Class<out T>): T {
        return JSON.parseObject(bytes, clazz)
    }
}
