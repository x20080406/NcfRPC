package personal.tianjie.rpc.client;

import personal.tianjie.rpc.Request;
import personal.tianjie.rpc.Response;
import personal.tianjie.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static personal.tianjie.rpc.Constants.SEND_TIMEOUT;
import static personal.tianjie.rpc.Constants.Status.ERROR;

/**
 * Created by tianjie on 4/9/15.
 */
public class NcfSyncProxy extends NcfProxy implements InvocationHandler {
    private static Logger LOGGER = LoggerFactory
            .getLogger(NcfSyncProxy.class);

    protected NcfSyncProxy() {
    }

    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        Request request = super.initRequest(method, args);

        NcfSyncProxy.LOGGER.debug("同步执行远程调用:{}", request);

        NcfRPCFuture<Response> future = super.invokeRemote(request);
        Response res = future.get(SEND_TIMEOUT, TimeUnit.SECONDS);
        if (res == null) {
            NcfSyncProxy.LOGGER.warn("从远程获取数据超时");
            throw new RpcException("从远程获取数据超时");
        }
        if (res.getStatus() == ERROR) {
            NcfSyncProxy.LOGGER.error("远程执行时出现异常", res.getCause());
            throw new RpcException(res.getCause());
        }
        return res.getResult();
    }


}
