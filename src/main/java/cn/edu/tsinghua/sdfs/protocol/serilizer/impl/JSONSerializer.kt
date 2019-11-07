package cn.edu.tsinghua.sdfs.protocol.serilizer.impl


import cn.edu.tsinghua.sdfs.protocol.serilizer.Serializer
import com.alibaba.fastjson.JSON

class JSONSerializer : Serializer {

    override fun serialize(`object`: Any): ByteArray {
        return JSON.toJSONBytes(`object`)
    }

    override fun <T> deserialize(bytes: ByteArray, clazz: Class<out T>): T {
        return JSON.parseObject(bytes, clazz)
    }
}
