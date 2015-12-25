package personal.tianjie.rpc.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.softee.management.annotation.Description;
import org.softee.management.annotation.MBean;
import org.softee.management.annotation.ManagedAttribute;
import org.softee.management.annotation.ManagedOperation;
import org.softee.management.helper.MBeanRegistration;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by tianjie on 4/15/15.
 */

@MBean(objectName = "personal.tianjie.rpc:type=Rpc,name=RpcRuntimeInfo")
@Description("This application shows how many tasks have been processed")
public final class RuntimeInfo {
    private Logger LOGGER = LoggerFactory.getLogger(RuntimeInfo.class);
    private final AtomicLong counter = new AtomicLong();

    public static RuntimeInfo getInstance() {
        return LazyHolder.INSTANCE;
    }

    private static class LazyHolder {
        private static final RuntimeInfo INSTANCE = new RuntimeInfo();

    }

    private RuntimeInfo() {
        try {
            new MBeanRegistration(this).register();
        } catch (Exception e) {
            LOGGER.error("组测MBean失败", e);
        }
    }

    public void increment() {
        this.counter.incrementAndGet();
    }

    @ManagedAttribute
    @Description("A counter variable")
    public long getCounter() {
        return counter.get();
    }

    @ManagedOperation
    @Description("Resets the counter")
    public void reset() {
        counter.set(0L);
    }

}
