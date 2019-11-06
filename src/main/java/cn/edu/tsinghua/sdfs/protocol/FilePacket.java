package cn.edu.tsinghua.sdfs.protocol;


import java.io.File;

import static cn.edu.tsinghua.sdfs.protocol.command.Command.FILE_PACKET;

public class FilePacket extends Packet {

    private File file;

    @Override
    public int getCommand() {
        return FILE_PACKET;
    }

    public FilePacket(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

}
