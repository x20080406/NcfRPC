package personal.tianjie.rpc.netty;

import personal.tianjie.rpc.Heartbeat;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.timeout.IdleState.READER_IDLE;
import static io.netty.handler.timeout.IdleState.WRITER_IDLE;

/**
 * Created by tianjie on 4/5/15.
 */
@Sharable
public class NcfIdleStateCheckHandler extends ChannelInboundHandlerAdapter {
    private Logger LOGGER = LoggerFactory.getLogger(
            NcfIdleStateCheckHandler.class);

    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if (msg == Heartbeat.getSingleton()) {
            LOGGER.trace("channel {},收到心跳包. ", ctx.channel());
            return;
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx,
                                   Object evt) throws Exception {
        //双方读写
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (READER_IDLE == event.state()) {
                LOGGER.debug("channel {} 超时", ctx.channel());
                ctx.channel().close();
            }
            if (WRITER_IDLE == event.state()) {
                LOGGER.debug("channel {},发送心跳.", ctx.channel());
                ctx.channel().writeAndFlush(Heartbeat.getSingleton());
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }

        //客户端写成功即可，手机段用比较适合，服务器端不用管，
        // 但是由于移动网络信号问题所以链接断开时间需要设置长一点并支持重连，
        // 另外写的心跳数据需要修改为空包。
        /*if (_evt instanceof IdleStateEvent) {
            LOGGER.info("空闲{}",_evt);
            ChannelFuture future = ctx.channel().writeAndFlush(
            Heartbeat.getSingleton());
            future.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        LOGGER.info("成功发送心跳到远程服务器，当前通道正常.");
                    } else {
                        LOGGER.warn("channel {} 超时",ctx.channel());
                        ctx.channel().close();
                    }
                }
            });
        } else {
            super.userEventTriggered(ctx, _evt);
        }*/
    }

}
