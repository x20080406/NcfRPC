package personal.tianjie.rpc.server;

import com.google.common.util.concurrent.Monitor;
import personal.tianjie.rpc.Constants;
import personal.tianjie.rpc.Request;
import personal.tianjie.rpc.Response;
import personal.tianjie.rpc.exception.RpcException;
import personal.tianjie.rpc.monitor.RuntimeInfo;
import personal.tianjie.rpc.util.SystemUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

/**
 * Created by tianjie on 4/4/15.
 */
@Sharable
public class NcfServerHandler extends ChannelInboundHandlerAdapter {
    private ExecutorService executorService;
    private Logger LOGGER = LoggerFactory.getLogger(NcfServerHandler.class);
    private final ConcurrentMap<String, Class<?>> cachedClazz =
            new ConcurrentHashMap<String, Class<?>>();
    private final Monitor monitor = new Monitor();

    public NcfServerHandler(ExecutorService executorService) {
        this.executorService = executorService;
    }


    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        RuntimeInfo.getInstance().increment();
        executorService.execute(new HandlerRunnable(ctx, msg));
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        LOGGER.error("exception was happened", cause);
    }

    class HandlerRunnable implements Runnable {
        private ChannelHandlerContext ctx;
        private Object message;

        public HandlerRunnable(ChannelHandlerContext ctx, Object message) {
            this.ctx = ctx;
            this.message = message;
        }

        public void run() {
            final Request req = (Request) message;
            Response response = new Response();
            response.setSerializer(req.getSerializer());
            response.setRequestId(req.getId());

            if (StringUtils
                    .startsWith(req.getTargetInterface(), "java.lang.")) {
                LOGGER.warn("不能调用{}的{}方法",
                        req.getTargetInterface(),
                        req.getMethodName());
                response.setStatus(Constants.Status.ERROR);
                response.setCause(new RpcException(
                        String.format("不能调用[%s]的[%s]方法",
                                req.getTargetInterface(),
                                req.getMethodName())));
                ctx.channel().writeAndFlush(response);
                return;
            }

            Class<?> targetClazz;
            if (!cachedClazz.containsKey(req.getTargetInterface())) {
                monitor.enter();
                try {
                    if (!cachedClazz.containsKey(req.getTargetInterface())) {
                        targetClazz = SystemUtil.getClassLoader(
                                HandlerRunnable.class)
                                .loadClass(req.getTargetInterface());

                        cachedClazz.putIfAbsent(req.getTargetInterface(),
                                targetClazz);
                    } else {
                        targetClazz = cachedClazz.get(
                                req.getTargetInterface());
                    }
                } catch (ClassNotFoundException e) {
                    LOGGER.error("类{}无法被加载", req.getTargetInterface(), e);
                    response.setCause(new RpcException(String
                            .format("类%s无法被加载", req.getTargetInterface())));
                    response.setStatus(Constants.Status.ERROR);
                    ctx.writeAndFlush(response);
                    return;
                } finally {
                    monitor.leave();
                }
            } else {
                targetClazz = cachedClazz.get(req.getTargetInterface());
            }

            try {
                ClassLoader cl = SystemUtil.getClassLoader(
                        HandlerRunnable.class);

                Class<?>[] paramTypes = SystemUtil.getClassTypeByName(cl,
                        req.getParameterTypeNames());

                Method method = targetClazz.getDeclaredMethod(
                        req.getMethodName(), paramTypes);

                Object obj = SpringContext.getInstance().getBean(targetClazz);
                Object rs = method.invoke(obj, req.getArgs());
                response.setResult(rs);
            } catch (Exception e) {
                LOGGER.error("调用{}的{}方法失败",
                        req.getTargetInterface(),
                        req.getMethodName(), e);
                response.setStatus(Constants.Status.ERROR);
                response.setCause(new RpcException(e.getMessage()));
            }

            ChannelFuture wf = ctx.channel().writeAndFlush(response);
            wf.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future)
                        throws Exception {
                    if (!future.isSuccess()) {
                        LOGGER.error("server write response error,request id is: "
                                + req.getId());
                        future.channel().close();
                    }
                }
            });

        }

    }
}
