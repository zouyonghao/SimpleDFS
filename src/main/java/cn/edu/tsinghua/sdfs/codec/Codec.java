package cn.edu.tsinghua.sdfs.codec;

import cn.edu.tsinghua.sdfs.protocol.FilePacket;
import cn.edu.tsinghua.sdfs.protocol.Packet;
import cn.edu.tsinghua.sdfs.protocol.serilizer.Serializer;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

import static cn.edu.tsinghua.sdfs.protocol.command.Command.FILE_PACKET;

public class Codec {

	public static final int TYPE = 0x12345678;

	private final Map<Integer, Class<? extends Packet>> packetTypeMap;

	public static Codec INSTANCE = new Codec();

	private Codec() {
		packetTypeMap = new HashMap<>();
		packetTypeMap.put(FILE_PACKET, FilePacket.class);
	}

	public ByteBuf encode(ByteBuf byteBuf, Packet packet) {
		byte[] bytes = Serializer.DEFAULT.serialize(packet);
		byteBuf.writeInt(TYPE);
		byteBuf.writeInt(packet.getCommand());
		byteBuf.writeInt(bytes.length);
		byteBuf.writeBytes(bytes);
		return byteBuf;
	}

	public Packet decode(ByteBuf byteBuf) {
		byteBuf.readInt();
		int command = byteBuf.readInt();
		int len = byteBuf.readInt();
		byte[] bytes = new byte[len];
		byteBuf.readBytes(bytes);

		Class<? extends Packet> clazz = packetTypeMap.get(command);
		if (clazz == null) {
			throw new NullPointerException("解析失败，没有该类型的数据包");
		}

		return Serializer.DEFAULT.<Packet>deserialize(bytes, clazz);

	}



}
