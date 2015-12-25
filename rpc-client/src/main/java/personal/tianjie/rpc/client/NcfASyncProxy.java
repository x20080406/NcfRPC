package personal.tianjie.rpc.client;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import personal.tianjie.rpc.Request;
import personal.tianjie.rpc.Response;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by tianjie on 4/9/15.
 */
public class NcfASyncProxy extends NcfProxy implements InvocationHandler {
    private static Logger LOGGER = LoggerFactory
            .getLogger(NcfSyncProxy.class);


    /**
     *
     * 组装请求，并发起远程调用.
     *
     * @param proxy 目标
     * @param method 方法
     * @param args 参数
     * @return 结果
     * @throws Throwable 异常
     */
    public final NcfRPCFuture<Response> invoke(final Object proxy,
                                               final Method method,
                                               final Object[] args)
            throws Throwable {
        Request request = super.initRequest(method, args);

        NcfASyncProxy.LOGGER.debug("异步执行远程调用:{}", request);

        return super.invokeRemote(request);
    }
}
