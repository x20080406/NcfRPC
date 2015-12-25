package personal.tianjie.rpc.server;

import personal.tianjie.rpc.Constants;
import personal.tianjie.rpc.codec.NcfDecoder;
import personal.tianjie.rpc.codec.NcfEncoder;
import personal.tianjie.rpc.netty.NcfIdleStateCheckHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by tianjie on 4/4/15.
 */
@Sharable
public class NcfServerChannelInitializer
        extends ChannelInitializer<SocketChannel> {

    private ExecutorService executorService;

    public NcfServerChannelInitializer(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline cp = ch.pipeline();
        cp.addLast("decoder", new NcfDecoder());
        cp.addLast("encoder", new NcfEncoder());
        cp.addLast("idleStateHandler",
                new IdleStateHandler(
                        Constants.HEARTBEAT_PERIOD * Constants.HEARTBEAT_THRESHOLD,
                        Constants.HEARTBEAT_PERIOD, 0,
                        TimeUnit.SECONDS));
        cp.addLast("ncfIdleStateCheckHandler", new NcfIdleStateCheckHandler());
        cp.addLast("business", new NcfServerHandler(executorService));
    }
}
