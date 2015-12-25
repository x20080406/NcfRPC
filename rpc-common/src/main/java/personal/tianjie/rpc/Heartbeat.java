package personal.tianjie.rpc;

/**
 * 心跳
 */
public final class Heartbeat extends Message {
    public static final byte[] BYTES = new byte[0];

    private static Heartbeat instance = new Heartbeat();

    public static Heartbeat getSingleton() {
        return instance;
    }

    private Heartbeat() {
    }
}
