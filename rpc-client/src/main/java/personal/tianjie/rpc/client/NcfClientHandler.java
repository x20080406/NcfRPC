package personal.tianjie.rpc.client;

import personal.tianjie.rpc.Response;
import personal.tianjie.rpc.monitor.RuntimeInfo;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static personal.tianjie.rpc.Constants.RECONNECT_DELAY;
import static personal.tianjie.rpc.Constants.RECONNECT_MAX_RETRY_TIMES;

/**
 * Created by tianjie on 4/4/15.
 */
public class NcfClientHandler extends ChannelInboundHandlerAdapter {
    private final static Logger LOGGER = LoggerFactory.getLogger(
            NcfClientHandler.class);

    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        RuntimeInfo.getInstance().increment();

        Response res = (Response) msg;
        NcfChannel channel = NcfConnectionManager.getInstance()
                .findNcfChannel(ctx.channel());
        NcfRPCFuture future = channel.getNcfConnectionManager()
                .getTaskManager().getTask(res.getRequestId());

        if (future == null) {
            LOGGER.warn("找不到为id{}的future", res.getRequestId());
            return;
        } else {
            future.commit(res);
        }
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }

    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    public void channelInactive(final ChannelHandlerContext context)
            throws Exception {
        if(!NcfConnectionManager.getInstance().isStarted()) return;

        LOGGER.info("连接已断开:{}，{}秒之后重新链接。",
                context.channel(), RECONNECT_DELAY);

        final NcfChannel channel = NcfConnectionManager
                .getInstance()
                .unavailableChannel(context.channel());

        EventLoop eventLoop = context.channel().eventLoop();
        eventLoop.schedule(new Runnable() {
            public void run() {
                reconnect(context, channel);
            }
        }, RECONNECT_DELAY, TimeUnit.SECONDS);
    }

    private static void reconnect(final ChannelHandlerContext context,
                                  final NcfChannel channel) {
        final int retryCounter = channel.retryCountDown();
        if (retryCounter < 0) {
            // close this broken channel
            // and cancel all waiting requests in this channel
            LOGGER.error("超过重连次数: {}", channel);
            return;
        }
        LOGGER.info("第{}次重连。", (RECONNECT_MAX_RETRY_TIMES - retryCounter));

        final Bootstrap newBootstrap = channel.getBootstrap().clone();

        ChannelFuture future = newBootstrap.connect();

        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    Channel newChannel = future.channel();
                    channel.setChannel(newChannel);
                    channel.setBootstrap(newBootstrap);
                    channel.resetRetryCounter();
                    NcfConnectionManager.getInstance()
                            .availableChannel(channel);
                    LOGGER.info("重新链接服务器成功: {}", channel);
                } else {
                    Throwable cause = future.cause();
                    StringBuilder msg = new StringBuilder("重新链接到服务器失败 {}");
                    if (retryCounter < 0) {
                        msg.append("，{}秒之后重新链接。");
                        LOGGER.error(msg.toString(),
                                channel,
                                RECONNECT_DELAY,
                                cause);
                    } else {
                        LOGGER.error(msg.toString(), cause);
                    }
                    /*LOGGER.warn("重新链接到服务器失败 {}"
                            + (retryCounter < 0 ? "" : "，{}秒之后重新链接。")
                            , channel
                            , (retryCounter < 0 ? "" : RECONNECT_DELAY)
                            , cause);*/

                    future.channel().eventLoop().schedule(new Runnable() {
                        public void run() {
                            reconnect(context, channel);
                        }
                    }, RECONNECT_DELAY, TimeUnit.SECONDS);
                }
            }
        });
    }
}
