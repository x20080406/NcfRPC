package personal.tianjie.rpc.client;

import com.google.common.collect.Maps;
import com.google.common.reflect.Reflection;
import personal.tianjie.rpc.RpcService;
import personal.tianjie.rpc.exception.ServiceNotFoundException;
import personal.tianjie.rpc.util.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static personal.tianjie.rpc.Constants.SCAN_PACKAGE;

/**
 * Created by tianjie on 4/4/15.
 */
public final class NcfClient {
    private static Logger LOGGER = LoggerFactory.getLogger(NcfClient.class);
    private static ConcurrentMap<Class, Object> syncCachedMap
            = Maps.newConcurrentMap();
    private static ConcurrentMap<Class, Object> asyncCachedMap
            = Maps.newConcurrentMap();
    //syncProxy of proxy-object
    private static NcfProxy syncProxy = new NcfSyncProxy();
    private static NcfProxy asyncProxy = new NcfASyncProxy();

    static {
        loadRpcService(SCAN_PACKAGE);
    }

    private NcfClient() {

    }

    private static void loadRpcService(String pkg) {
        Set<String> services = SystemUtil
                .scanAnnotationObject(RpcService.class, pkg);

        for (String clazzName : services) {
            Class clazz;
            try {
                clazz = SystemUtil
                        .getClassLoader(NcfClient.class)
                        .loadClass(clazzName);

                if (!clazz.isInterface()) {
                    LOGGER.warn("[{}]不是接口类型,将被忽略.", clazzName);
                    continue;
                }
                LOGGER.info("load rpc stub for {}", clazzName);
            } catch (ClassNotFoundException e) {
                LOGGER.error("类[{}]无法被加载", clazzName, e);
                continue;
            }
            if(!syncCachedMap.containsKey(clazz)) {
                Object syncObj = Reflection.newProxy(clazz, syncProxy);
                syncCachedMap.put(clazz, syncObj);
            }
            if(!asyncCachedMap.containsKey(clazz)) {
                Object asyncObj = Reflection.newProxy(clazz, asyncProxy);
                asyncCachedMap.put(clazz, asyncObj);
            }
        }
    }

    /**
     * 返回接口的同步代理类
     *
     * @param interfaceClazz
     * @return
     */
    public static <T extends Class, E> E getSyncService(T interfaceClazz)
            throws ServiceNotFoundException {
        checkService(interfaceClazz);
        return (E) syncCachedMap.get(interfaceClazz);
    }

    /**
     * 返回接口的异步代理类<br>
     * 注意：异步代理的任何方法返回的值都是{@link NcfRPCFuture},<br/>
     * 通过调用{@link
     * NcfRPCFuture}的get方法获取值
     *
     * @param interfaceClazz
     * @return
     */
    public static <T extends Class, E> E getASyncService(T interfaceClazz)
            throws ServiceNotFoundException {
        checkService(interfaceClazz);
        return (E) asyncCachedMap.get(interfaceClazz);
    }

    /**
     * 获取接口的代理类
     *
     * @param interfaceClazz
     * @param pkg
     * @return
     */
    public static <T extends Class, E> E getSyncService(T interfaceClazz,
                                                        String pkg)
            throws ServiceNotFoundException {
        if (!syncCachedMap.containsKey(interfaceClazz)) {
            loadRpcService(pkg);
        }
        checkService(interfaceClazz);
        return (E) syncCachedMap.get(interfaceClazz);
    }

    /**
     * 返回异步调用代理。代理返回的结果为RpcFuture
     *
     * @param interfaceClazz
     * @param pkg
     * @return
     */
    public static <T extends Class, E> E getASyncService(T interfaceClazz,
                                                         String pkg)
            throws ServiceNotFoundException {
        if (!asyncCachedMap.containsKey(interfaceClazz)) {
            loadRpcService(pkg);
        }
        checkService(interfaceClazz);
        return (E) asyncCachedMap.get(interfaceClazz);
    }

    private static <T extends Class<?>> void checkService(T interfaceClazz)
            throws ServiceNotFoundException {
        if (!interfaceClazz.isInterface()) {
            throw new ServiceNotFoundException("当前服务class不是接口类型")
                    .addContextValue("serviceName", interfaceClazz.getName());
        }
        if (!interfaceClazz.isAnnotationPresent(RpcService.class)) {
            throw new ServiceNotFoundException("当前服务没有被RpcService注解标注")
                    .addContextValue("serviceName", interfaceClazz.getName());
        }
        if (!asyncCachedMap.containsKey(interfaceClazz)) {
            throw new ServiceNotFoundException("当前服务没有被框架扫描到")
                    .addContextValue("serviceName", interfaceClazz.getName());
        }
    }
}
