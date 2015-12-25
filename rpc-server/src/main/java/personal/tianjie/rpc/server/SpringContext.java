package personal.tianjie.rpc.server;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by tianjie on 4/11/15.
 */
public final class SpringContext {
    private static final String DEFAULT_CFG_FILE = "classpath*:ncf-rpc-cfg.xml";

    public static ApplicationContext getInstance() {
        return LazyHolder.INSTANCE;
    }

    private static class LazyHolder {
        private static final ApplicationContext INSTANCE =
                new ClassPathXmlApplicationContext(DEFAULT_CFG_FILE);
    }
}
