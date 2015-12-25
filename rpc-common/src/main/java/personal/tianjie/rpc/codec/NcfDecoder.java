package personal.tianjie.rpc.codec;

import personal.tianjie.rpc.Heartbeat;
import personal.tianjie.rpc.serializer.NcfSerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static personal.tianjie.rpc.Constants.HEADER_SIZE;
import static personal.tianjie.rpc.Constants.NO_BODY_LEN;

/**
 * 完整数据由header和body组成<br/> header总长为5,
 * 由body length（1个int） + serializer（1个byte）组成<br/>
 * body为二进制数据
 * Created by tianjie on 4/4/15.
 */
public class NcfDecoder extends ByteToMessageDecoder {
    private Logger LOGGER = LoggerFactory.getLogger(NcfDecoder.class);

    protected void decode(ChannelHandlerContext ctx,
                          ByteBuf in,
                          List<Object> out)
            throws Exception {
        if (in.readableBytes() >= HEADER_SIZE) {
            in.markReaderIndex();
            //读4个字节，body长度
            int bodyLength = in.readInt();
            //读1个字节，序列化类型
            byte serializer = in.readByte();
            if (bodyLength == NO_BODY_LEN) {
                LOGGER.trace("[{}]收到的数据长度为0,视为心跳包.", ctx.channel());
                out.add(Heartbeat.getSingleton());
                return;
            } else if (bodyLength < NO_BODY_LEN) {
                LOGGER.error("channel[{}]'s message length must >=0,but = {}",
                        ctx.channel(), bodyLength);
                return;
            } else if (NcfSerializerFactory.getSerializer(serializer) == null) {
                LOGGER.error("unknown serializer type {}", bodyLength);
                return;
            } else if (in.readableBytes() >= bodyLength) {
                byte[] bytes = new byte[bodyLength];
                in.readBytes(bytes);
                Object obj = NcfSerializerFactory.getSerializer(serializer)
                        .decode(bytes);
                out.add(obj);
            } else {
                in.resetReaderIndex();
            }
        }
    }
}
