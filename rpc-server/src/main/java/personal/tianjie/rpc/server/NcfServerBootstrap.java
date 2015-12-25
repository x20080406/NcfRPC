package personal.tianjie.rpc.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import personal.tianjie.rpc.Constants;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Calendar;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tianjie on 4/23/15.
 */
public class NcfServerBootstrap {
    private static Logger LOGGER = LoggerFactory
            .getLogger(NcfServerBootstrap.class);

    private static String EXECUTOR_SERVICE="NCF-RPC-EXECUORGROUP",
                                WORKS_GROUP="NCF-RPC-WORKERGROUP",
                                BOSS_GROUP="NCF-RPC-BOSSGROUP";

    /**
     * 初始化并将spring context设置到SpringUtil中
     */
    static {
        ApplicationContext ctx = SpringContext.getInstance();
        long val = Calendar.getInstance().getTimeInMillis()
                - ctx.getStartupDate();
        LOGGER.info("初始化spring bean factory耗时:{}毫秒", val);
    }

    private boolean addShutdownHook = false;
    private NcfServerConfig config;

    private ExecutorService executorService;
    private CountDownLatch countDownLatch;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    /**
     * 是否注册停止系统的钩子程序
     *
     * @param addShutdownHook
     */
    public void addShutdownHook(boolean addShutdownHook) {
        this.addShutdownHook = addShutdownHook;
    }

    public NcfServerBootstrap(NcfServerConfig config) {
        this.config = config;
    }

    public NcfServerBootstrap(NcfServerConfig config,
                              CountDownLatch countDownLatch) {
        this.config = config;
        this.countDownLatch = countDownLatch;
    }

    public synchronized void start() {
        executorService = Executors.newFixedThreadPool(
                config.getEvtExecutorSize(),
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat(EXECUTOR_SERVICE)
                        .build());

        bossGroup = new NioEventLoopGroup(config.getBossExecutorSize(),
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat(BOSS_GROUP)
                        .build());
        workerGroup = new NioEventLoopGroup(config.getEvtExecutorSize(),
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat(WORKS_GROUP)
                        .build());
        try {
            ServerBootstrap b = initServer();
            ChannelFuture f = b.bind(config.getPort()).sync();
            LOGGER.info("server started at port: {} ", config.getPort());
            addShutdownHook();
            notifyInvoker();
            //阻塞
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.error("服务器异常", e);
        }
    }

    private ServerBootstrap initServer() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new NcfServerChannelInitializer(executorService))
                .option(ChannelOption.SO_BACKLOG, Constants.SO_BLOCKING)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        return serverBootstrap;
    }

    /**
     * 通知主线程启动完毕
     */
    private void notifyInvoker() {
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }

    /**
     * 关闭系统
     */
    public void shutdownGracefully() {
        executorService.shutdown();
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        LOGGER.info("服务器已关闭.");
    }

    /**
     * 用于停止系统的钩子
     */
    private void addShutdownHook() {
        if (addShutdownHook) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    shutdownGracefully();
                }
            });
        }
    }
}
