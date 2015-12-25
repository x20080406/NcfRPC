package personal.tianjie.rpc;


import personal.tianjie.rpc.serializer.NcfSerializer;

import java.io.Serializable;

/**
 * Created by tianjie on 4/4/15.
 */
public abstract class Message implements Serializable {
    /**
     * 版本
     */
    private byte version = (byte) 1;
    /**
     * 调用方式：0同步，1异步
     */
    private byte type = (byte) 0;  //0 sync, 1 async

    /**
     * 参见{@link NcfSerializer}
     */
    private byte serializer = Constants.Codec.HESSIAN;

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getSerializer() {
        return serializer;
    }

    public void setSerializer(byte serializer) {
        this.serializer = serializer;
    }
}
