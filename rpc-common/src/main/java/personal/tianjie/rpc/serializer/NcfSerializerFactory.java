package personal.tianjie.rpc.serializer;


import personal.tianjie.rpc.Constants;
import personal.tianjie.rpc.exception.SerializeException;

/**
 * Created by tianjie on 4/4/15.
 */
public final class NcfSerializerFactory {
    /**
     * 0->empty
     * 1->hessian
     * 2->kryo
     */
    private static NcfSerializer[] serializers =
            new NcfSerializer[Constants.SERIALIZER_SIZE];

    private NcfSerializerFactory() {
    }

    static {
        serializers[Constants.Codec.HESSIAN] = new HessianSerializer();
        serializers[Constants.Codec.KRYO] = new KryoSerializer();
    }

    /**
     * 获取Serializer
     *
     * @param type
     * @return
     */
    public static NcfSerializer getSerializer(byte type) {
        if (type <= 0 || type > serializers.length
                || serializers[type] == null) {
            throw new SerializeException("不支持的序列化类型");
        }
        return serializers[type];
    }
}
