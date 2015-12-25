package personal.tianjie.rpc.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tianjie on 9/16/15.
 */
public class KryoSerializer implements NcfSerializer {
    KryoPool pool = new KryoPool.Builder(new KryoFactory() {
        public Kryo create() {
            Kryo kryo = new Kryo();
            //config here
            return kryo;
        }
    }).softReferences().build();

    public <T extends Serializable> byte[] encode(T message) throws Exception {
        if (message == null) return null;

        Kryo kryo = pool.borrow();

        try (ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteOutputStream)) {
            kryo.writeClassAndObject(output, message);
            return output.getBuffer();
        } finally {
            pool.release(kryo);
        }
    }

    public <T> T decode(byte[] bytes) throws Exception {
        if (bytes == null) return null;
        Kryo kryo = pool.borrow();
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(inputStream)) {
            return (T) kryo.readClassAndObject(input);
        } finally {
            pool.release(kryo);
        }
    }

}
