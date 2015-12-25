package personal.tianjie.rpc.exception;

/**
 * Created by tianjie on 4/28/15.
 */
public class ServiceNotFoundException extends RpcException {
    public ServiceNotFoundException(String message) {
        super(message);
    }

    public ServiceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceNotFoundException(Throwable cause) {
        super(cause);
    }

    public ServiceNotFoundException addContextValue(String label,
                                                    Object value) {
        super.addContextValue(label, value);
        return this;
    }
}
