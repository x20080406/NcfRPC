package personal.tianjie.rpc.client;

import personal.tianjie.rpc.codec.NcfDecoder;
import personal.tianjie.rpc.codec.NcfEncoder;
import personal.tianjie.rpc.netty.NcfIdleStateCheckHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.concurrent.TimeUnit;

import static personal.tianjie.rpc.Constants.HEARTBEAT_PERIOD;
import static personal.tianjie.rpc.Constants.HEARTBEAT_THRESHOLD;


/**
 * Created by tianjie on 4/4/15.
 */
public class NcfClientChannelInitializer
        extends ChannelInitializer<SocketChannel> {
    private final EventExecutorGroup executorGroup;
    /**
     * The indication of whether stream compression will be enabled.
     */
    private boolean compression = false;

    public NcfClientChannelInitializer(EventExecutorGroup executorGroup,
                                       boolean compression) {
        this.executorGroup = executorGroup;
        this.compression = compression;
    }

    public NcfClientChannelInitializer(EventExecutorGroup executorGroup) {
        this(executorGroup, false);
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline cp = ch.pipeline();
        if (this.compression) {
            // Enable stream compression
            cp.addLast(this.executorGroup, "deflater",
                    ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP));
            cp.addLast(this.executorGroup, "inflater",
                    ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));
        }

        cp.addLast(executorGroup, "decoder", new NcfDecoder());
        cp.addLast(executorGroup, "encoder", new NcfEncoder());
        cp.addLast(executorGroup, "idleStateHandler",
                   new IdleStateHandler(HEARTBEAT_PERIOD * HEARTBEAT_THRESHOLD,
                           HEARTBEAT_PERIOD, 0,
                           TimeUnit.SECONDS));
        cp.addLast(executorGroup, "ncfIdleStateCheckHandler",
                new NcfIdleStateCheckHandler());
        cp.addLast(executorGroup, "handler",
                new NcfClientHandler());
    }
}
