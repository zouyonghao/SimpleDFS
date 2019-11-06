package cn.edu.tsinghua.sdfs.protocol.serilizer.impl;


import cn.edu.tsinghua.sdfs.protocol.serilizer.Serializer;
import com.alibaba.fastjson.JSON;

public class JSONSerializer implements Serializer {

    @Override
    public byte[] serialize(Object object) {
        return JSON.toJSONBytes(object);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<? extends T> clazz) {
        return JSON.parseObject(bytes, clazz);
    }
}
