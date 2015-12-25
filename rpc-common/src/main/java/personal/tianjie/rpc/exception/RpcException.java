package personal.tianjie.rpc.exception;

import org.apache.commons.lang3.exception.ContextedException;

/**
 * Created by tianjie on 4/9/15.
 */
public class RpcException extends ContextedException {
    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }

    public RpcException addContextValue(String label, Object value) {
        super.addContextValue(label, value);
        return this;
    }
}
