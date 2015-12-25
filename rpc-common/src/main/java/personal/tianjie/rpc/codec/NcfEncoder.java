package personal.tianjie.rpc.codec;

import personal.tianjie.rpc.Constants;
import personal.tianjie.rpc.Heartbeat;
import personal.tianjie.rpc.Message;
import personal.tianjie.rpc.serializer.NcfSerializer;
import personal.tianjie.rpc.serializer.NcfSerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 完整数据由header和body组成<br/>
 * header总长为5,由body length（1个int） + serializer（1个byte）组成<br/>
 * body为二进制数据
 * Created by tianjie on 4/4/15.
 */
public class NcfEncoder extends ChannelOutboundHandlerAdapter {
    private Logger LOGGER = LoggerFactory.getLogger(NcfEncoder.class);

    public void write(ChannelHandlerContext ctx, Object msg,
                      ChannelPromise promise)
            throws Exception {
        if (msg instanceof Message) {
            Message message = (Message) msg;
            ByteBuf buf;
            if (message instanceof Heartbeat) {
                buf = ctx.alloc().buffer(Constants.HEADER_SIZE);
                buf.writeInt(Constants.NO_BODY_LEN);
                buf.writeByte(message.getSerializer());
                LOGGER.trace("发送心跳：{}", buf);
            } else {
                NcfSerializer s = NcfSerializerFactory
                        .getSerializer(message.getSerializer());
                byte[] body = s.encode(message);
                buf = ctx.alloc().buffer(Constants.HEADER_SIZE + body.length);
                buf.writeInt(body.length);
                buf.writeByte(message.getSerializer());
                buf.writeBytes(body);
                LOGGER.debug("发送数据：{}", buf);
            }
            ctx.writeAndFlush(buf);
        }
    }
}
