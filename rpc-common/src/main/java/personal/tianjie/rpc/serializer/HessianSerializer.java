package personal.tianjie.rpc.serializer;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import personal.tianjie.rpc.exception.SerializeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by tianjie on 4/4/15.
 */
public class HessianSerializer implements NcfSerializer {
    private Logger LOGGER = LoggerFactory.getLogger(HessianSerializer.class);

    public <T extends Serializable> byte[] encode(T message) {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(byteArray);
        try {
            output.writeObject(message);
            output.close();
            return byteArray.toByteArray();
        } catch (Exception e) {
            throw new SerializeException("序列化出错", e);
        } finally {
            try {
                output.close();
            } catch (IOException e) {
                LOGGER.warn("关闭输出流出错", e);
            }
        }
    }

    public <T> T decode(byte[] bytes) throws Exception {
        Hessian2Input input = new Hessian2Input(
                new ByteArrayInputStream(bytes));
        try {
            Object resultObject = input.readObject();
            return (T) resultObject;
        } catch (Exception e) {
            throw new SerializeException("反序列化出错", e);
        } finally {
            input.close();
        }
    }
}
