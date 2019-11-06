package cn.edu.tsinghua.sdfs.protocol.serilizer;


import cn.edu.tsinghua.sdfs.protocol.serilizer.impl.JSONSerializer;

public interface Serializer {

	Serializer DEFAULT = new JSONSerializer();

	byte[] serialize(Object object);

	<T> T deserialize(byte[] bytes, Class<? extends T> clazz);

}
