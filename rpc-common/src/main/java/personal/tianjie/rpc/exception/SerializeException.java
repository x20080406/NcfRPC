package personal.tianjie.rpc.exception;

/**
 * Created by tianjie on 4/4/15.
 */
public class SerializeException extends RuntimeException {
    public SerializeException(Throwable throwable) {
        super(throwable);
    }

    public SerializeException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public SerializeException(String message) {
        super(message);
    }

}
