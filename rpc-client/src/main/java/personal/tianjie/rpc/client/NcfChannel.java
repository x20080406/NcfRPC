package personal.tianjie.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

import static personal.tianjie.rpc.Constants.RECONNECT_MAX_RETRY_TIMES;

/**
 * Created by tianjie on 4/6/15.
 */
public class NcfChannel {
    private final int id;
    private Bootstrap bootstrap;
    private Channel channel;
    private NcfConnectionManager ncfConnectionManager;

    /**
     * 默认300次
     */
    private int retryCounter = RECONNECT_MAX_RETRY_TIMES;

    public NcfChannel(int id, Channel channel,
                      NcfConnectionManager ncfConnectionManager,
                      Bootstrap bootstrap) {
        this.id = id;
        this.channel = channel;
        this.bootstrap = bootstrap;
        this.ncfConnectionManager = ncfConnectionManager;
    }

    public NcfConnectionManager getNcfConnectionManager() {
        return ncfConnectionManager;
    }

    public synchronized void resetRetryCounter() {
        retryCounter = RECONNECT_MAX_RETRY_TIMES;
    }

    public synchronized int retryCountDown() {
        return --retryCounter;
    }

    public int getId() {
        return id;
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        String format = String.format("ClientChannel-%02d", this.id);
        StringBuilder result = new StringBuilder();
        result.append(format);
        result.append('/');
        result.append(channel);
        return result.toString();
    }
}
