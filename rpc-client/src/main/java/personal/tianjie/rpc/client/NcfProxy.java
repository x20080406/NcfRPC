package personal.tianjie.rpc.client;

import personal.tianjie.rpc.Request;
import personal.tianjie.rpc.Response;
import personal.tianjie.rpc.exception.RpcException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by tianjie on 4/15/15.
 */
public abstract class NcfProxy implements InvocationHandler {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(NcfProxy.class);

    protected NcfProxy() {}

    protected NcfRPCFuture invokeRemote(Request request) throws RpcException {
        NcfChannel cf = NcfConnectionManager.getInstance().selectChannel();
        NcfRPCFuture<Response> future = new NcfRPCFuture(
                request.getId(),
                cf.getNcfConnectionManager().getTaskManager());
        try {
            cf.getNcfConnectionManager()
                    .getTaskManager()
                    .addTask(request.getId(), future);
            cf.getChannel().writeAndFlush(request);
        } catch (Exception e) {
            future.cancel(true);
            throw new RpcException("调用失败", e);
        }
        return future;
    }

    protected Request initRequest(Method method, Object[] args)
            throws RpcException {
        if (StringUtils.startsWith(method.getDeclaringClass().getName(),
                "java.lang.")) {
            throw new RpcException(String.format("不能调用%s的%s方法",
                    method.getDeclaringClass().getName(),
                    method.getDeclaringClass().getName()));
        }

        Request request = new Request();

        String[] typeNames = new String[method.getParameterTypes().length];
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            Class clazz = method.getParameterTypes()[i];
            typeNames[i] = clazz.getName();
        }
        request.setParameterTypeNames(typeNames);
        request.setArgs(args);
        request.setMethodName(method.getName());
        request.setTargetInterface(method.getDeclaringClass().getName());
        LOGGER.debug("构造Request[{}]", request);
        return request;
    }
}
