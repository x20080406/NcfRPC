package personal.tianjie.rpc.client;

import com.google.common.util.concurrent.Monitor;
import personal.tianjie.rpc.Constants;
import personal.tianjie.rpc.Response;
import personal.tianjie.rpc.exception.RpcException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Integer.MAX_VALUE;

/**
 * Created by tianjie on 4/6/15.
 */
public final class NcfConnectionManager {
    private static Logger LOGGER = LoggerFactory
            .getLogger(NcfConnectionManager.class);
    private TaskManager taskManager = new TaskManager();

    /**
     * 获取任务管理器
     *
     * @return
     */
    public TaskManager getTaskManager() {
        return taskManager;
    }

    /**
     * 并发控制
     */
    private Monitor monitor = new Monitor();


    /**
     * 活动连接池
     */
    private final ConcurrentMap<Integer, NcfChannel> channelPool =
            new ConcurrentHashMap<Integer, NcfChannel>();

    /**
     * 重连
     */
    private final ConcurrentMap<Integer, NcfChannel> reconnectChannelPool =
            new ConcurrentHashMap<Integer, NcfChannel>();

    /**
     * 启动标志
     */
    private volatile boolean started = false;

    /**
     * netty 事件组
     */
    private EventExecutorGroup executorGroup;
    private NioEventLoopGroup eventLoopGroup;

    /**
     * 索引标志位
     */
    private AtomicInteger channelIndex = new AtomicInteger(0);

    private static class LazyHolder {
        private static final NcfConnectionManager INSTANCE =
                new NcfConnectionManager();
    }

    /**
     * 获取链接管理器实例
     *
     * @return
     */
    public static NcfConnectionManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    private Integer nextIndex() throws RpcException {
        int poolSize = this.channelPool.size();
        if (poolSize == 0) {
            throw new RpcException("没有可用的链接");
        }
        if (channelIndex.get() == MAX_VALUE) {
            channelIndex.compareAndSet(MAX_VALUE, 0);
        }
        int curt = channelIndex.incrementAndGet();
        return curt % poolSize;
    }

    /**
     * 将连接置为不可用
     *
     * @param channel
     */
    protected NcfChannel unavailableChannel(Channel channel) {
        monitor.enter();
        try {
            NcfChannel result = null;
            Collection<NcfChannel> proxyCol = this.channelPool.values();
            for (NcfChannel proxy : proxyCol) {
                if (channel.equals(proxy.getChannel())) {
                    result = proxy;
                    break;
                }
            }
            channelPool.remove(result.getId());
            reconnectChannelPool.put(result.getId(), result);
            return result;
        } finally {
            monitor.leave();
        }
    }

    /**
     * 将连接置为不可用
     *
     * @param channel
     */
    protected NcfChannel availableChannel(NcfChannel channel) {
        monitor.enter();
        try {
            reconnectChannelPool.remove(channel.getId());
            channelPool.put(channel.getId(), channel);
            return channel;
        } finally {
            monitor.leave();
        }
    }

    /**
     * 根据netty channel查找连接池中封装的对象
     *
     * @param channel
     * @return
     */
    protected NcfChannel findNcfChannel(Channel channel) {
        if (channel == null) {
            return null;
        }

        NcfChannel result = null;
        Collection<NcfChannel> proxyCol = this.channelPool.values();
        for (NcfChannel proxy : proxyCol) {
            if (channel.equals(proxy.getChannel())) {
                result = proxy;
                break;
            }
        }

        return result;
    }

    /**
     * 从连接池获取一个连接
     *
     * @return
     * @throws RpcException
     */
    public NcfChannel selectChannel() throws RpcException {
        if (!started)
            throw new RpcException("未启动客户端");

        NcfChannel result;
        do {
            Integer index = this.nextIndex();
            if (index < 0) {
                throw new RpcException("获取连接的下标错误。")
                        .addContextValue("index", index);
            }
            result = this.channelPool.get(index);

            //会阻塞EventExecutor，所以上面改为直接抛异常，快速返回错误
            /*if (selectCounter <= 0) {
                this.monitor.enter();
                try {
                    this.monitor.waitFor(selectGuard);
                } catch (InterruptedException e) {
                    LOGGER.error("获取连接错误", e);
                } finally {
                    this.monitor.leave();
                }

                selectCounter = this.channelPool.size();
            }*/
        } while (null == result);
        LOGGER.debug("获取连接：{}", result);
        return result;
    }

    public void shutdown() {
        executorGroup.shutdownGracefully();
        eventLoopGroup.shutdownGracefully();
        LOGGER.info("客户端已关闭.");
    }

    public synchronized boolean start(List<NcfAddress> remoteServerLists) {
        if (started) {
            LOGGER.info("客户端已启动，无需多次重启。");
            return started;
        }

        LOGGER.info("正在启动客户端...");
        int evtExecutorSize = Constants.EVT_EXECUTOR_SIZE;
        int channelNum = Constants.CHANNEL_NUM;
        executorGroup = new DefaultEventExecutorGroup(evtExecutorSize);
        eventLoopGroup = new NioEventLoopGroup();

        Channel channel;
        NcfChannel ncfChannel;
        Bootstrap bootstrap = null;
        ChannelFuture future;
        int id = 0;

        for (NcfAddress remoteServer : remoteServerLists) {
            for (int i = 0; i < channelNum; i++) {
                if (bootstrap == null) {
                    bootstrap = new Bootstrap();

                    bootstrap.group(eventLoopGroup)
                            .channel(NioSocketChannel.class);

                    bootstrap.remoteAddress(
                            new InetSocketAddress(
                                    remoteServer.getHost(),
                                    remoteServer.getPort()));

                    NcfClientChannelInitializer initializer =
                            new NcfClientChannelInitializer(
                                    executorGroup);

                    bootstrap.handler(initializer);

                    bootstrap.option(
                            ChannelOption.CONNECT_TIMEOUT_MILLIS,
                            Constants.CONNECT_TIMEOUT
                                    * Constants.SECONDS_HOLDER);

                } else {
                    bootstrap = bootstrap.clone();
                    bootstrap.remoteAddress(
                            new InetSocketAddress(
                                    remoteServer.getHost(),
                                    remoteServer.getPort()));
                }
                future = bootstrap.connect().awaitUninterruptibly();

                if (future.isDone()) {
                    if (future.isSuccess()) {
                        channel = future.channel();
                        ncfChannel = new NcfChannel(id++,
                                channel,
                                this, bootstrap);
                        channelPool.put(ncfChannel.getId(), ncfChannel);
                        this.started = true;
                        LOGGER.info("Channel:[{}]连接成功", ncfChannel);

                    } else {
                        id -= 1;
                        Throwable cause = future.cause();
                        LOGGER.error("server[{}:{}]连接失败",
                                remoteServer.getHost(),
                                remoteServer.getPort(),
                                cause);
                    }
                }
            }

        }
        started = true;
        return started;
    }

    public boolean isStarted() {
        return started;
    }

    class TaskManager {
        private final ConcurrentMap<Long, NcfRPCFuture<Response>> rpcJobs =
                new ConcurrentHashMap<Long, NcfRPCFuture<Response>>();
        private final Semaphore maxTasks =
                new Semaphore(Constants.MAX_TASK_SIZE);

        public NcfRPCFuture getTask(Long taskId) {
            return this.rpcJobs.get(taskId);
        }

        public void addTask(Long taskId, NcfRPCFuture task)
                throws RpcException {
            try {
                maxTasks.acquire();
                this.rpcJobs.put(taskId, task);
            } catch (InterruptedException e) {
                throw new RpcException("无法获取信号", e);
            }
        }

        public void removeTask(Long taskId) {
            maxTasks.release();
            this.rpcJobs.remove(taskId);
        }

        public ConcurrentMap<Long, NcfRPCFuture<Response>> getRpcJobs() {
            return rpcJobs;
        }

    }
}
