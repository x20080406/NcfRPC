package personal.tianjie.rpc;

/**
 * Created by tianjie on 4/4/15.
 */
public class Response extends Message {
    private long requestId;
    private Object result;
    private byte status = Constants.Status.OK;
    private Exception cause;

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public Exception getCause() {
        return cause;
    }

    public void setCause(Exception cause) {
        this.cause = cause;
    }
}
