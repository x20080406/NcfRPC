package personal.tianjie;

import personal.tianjie.rpc.Constants;
import personal.tianjie.rpc.client.NcfAddress;
import personal.tianjie.rpc.client.NcfConnectionManager;
import personal.tianjie.rpc.server.NcfServerBootstrap;
import personal.tianjie.rpc.server.NcfServerConfig;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 用法：<br> public void test() throws InterruptedException {<br/>
 * &nbsp;&nbsp;HelloService service =
 * NcfClient.getSyncService(HelloService.class);<br/>
 * &nbsp;&nbsp;System.out.println(service.sendMail("test"+i));<br/>
 * &nbsp;&nbsp;}<br/> 参数说明： 在junit启动时加上以下vm参数实现服务器地址修改：-Dss=false -Dhosts=10.20.4.244:23432<br> -Dss
 * 是否启动服务器，默认为true，表示本地要启动服务，仍需指定地址，默认地址：127.0.0.1:23432。如果ss为false，必须指定hosts<br/> -Dhosts 服务器地址，格式ip:port<br>
 * Created by tianjie on 4/27/15.
 */
public abstract class NcfAbstractCaseBase {
    protected static Logger LOGGER = LoggerFactory.getLogger("RpcTest");
    //执行junit时可以在vmoption中添加hosts=127.0.0.1:8080,127.0.0.1:9090方法来指定host，通过ss来决定是否启动服务器.值为true/false
    final static String P_HOSTS = "hosts";
    final static String P_START_SERVER_FLAG = "ss";

    static NcfServerBootstrap bootstrap;
    static NcfConnectionManager connectionManager;

    static boolean START_SERVER_FLAG = true;
    static List<NcfAddress> SERVER_HOSTS = new ArrayList<NcfAddress>(1);

    static {
        SERVER_HOSTS.add(new NcfAddress("127.0.0.1", Constants.SERVER_PORT));
    }

    /**
     * 为保证环境正确初始化，不允许覆盖init方法，派生类另外起名，并用{@link BeforeClass}标注
     */
    @BeforeClass
    public static final void init() {
        String startServer = System.getProperty(P_START_SERVER_FLAG);
        if (StringUtils.equalsIgnoreCase("false", startServer)) {
            START_SERVER_FLAG = false;
            SERVER_HOSTS.clear();
            String hostString = System.getProperty(P_HOSTS);
            String[] hosts = hostString.split("\\|");
            for (String host : hosts) {
                String[] hostInfo = host.split(":");
                SERVER_HOSTS.add(new NcfAddress(hostInfo[0], Integer.valueOf(hostInfo[1])));
            }
        } else if (StringUtils.equalsIgnoreCase("true", startServer)) {
            START_SERVER_FLAG = true;
        }
        LOGGER.info("执行初始化");
        if (START_SERVER_FLAG) {
            //启动server
            final CountDownLatch cdl = new CountDownLatch(1);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(new Runnable() {
                public void run() {
                    bootstrap = new NcfServerBootstrap(new NcfServerConfig(), cdl);
                    bootstrap.start();
                }
            });
            try {
                cdl.await();
            } catch (InterruptedException e) {
                LOGGER.error("发生异常", e);
            }
        }

        //启动client
        connectionManager = NcfConnectionManager.getInstance();
        connectionManager.start(SERVER_HOSTS);
    }

    /**
     * 为保证环境正确关闭，不允许覆盖desotry方法，派生类另外起名，并用{@link AfterClass}标注
     */
    @AfterClass
    public final static void desotry() {
        connectionManager.shutdown();
        if (START_SERVER_FLAG) {
            bootstrap.shutdownGracefully();//钩子关闭，此处不用理会
        }
    }


}
