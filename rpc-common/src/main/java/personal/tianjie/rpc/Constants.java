package personal.tianjie.rpc;

import java.io.Serializable;

/**
 * Created by tianjie on 4/4/15.
 */
public interface Constants extends Serializable {

    /**
     * 扫描包下的Rpc服务
     */
    final String SCAN_PACKAGE = "personal.tianjie";

    /**
     * 服务器端口
     */
    final int SERVER_PORT = 23432;

    /**
     * 包头长度
     */
    final int HEADER_SIZE = 5;

    /**
     * 没有数据时占位符
     */
    final int NO_BODY_LEN=0;

    /**
     * 状态
     */
    interface Status extends Serializable {
        byte OK = (byte) 1;
        byte ERROR = (byte) 0;
    }

    /**
     * 编码类型
     */
    interface Codec extends Serializable {
        byte HESSIAN = (byte) 1;
        byte KRYO = (byte) 2;
    }

    /**
     * 心跳时间周期
     */
    final static int HEARTBEAT_PERIOD = 5;

    /**
     * 读写比例
     */
    final static int HEARTBEAT_THRESHOLD = 3;

    /**
     * 重试间隔（秒）
     */
    final static int RECONNECT_DELAY = 15;

    /**
     * 重试次数
     */
    final static int RECONNECT_MAX_RETRY_TIMES = Integer.MAX_VALUE;

    /**
     * 事件处理线程
     */
    final static int EVT_EXECUTOR_SIZE = Runtime.getRuntime()
            .availableProcessors() * 4;

    /**
     * 事件处理线程
     */
    final static int BOSS_EXECUTOR_SIZE = Runtime.getRuntime()
            .availableProcessors() ;

    /**
     * 每个地址打开连接数
     */
    final static int CHANNEL_NUM = Runtime.getRuntime()
            .availableProcessors() * 2;
    /**
     * 连接超时时间
     */
    final static int CONNECT_TIMEOUT = 60;

    /**
     * 接收超时
     */
    final static int SEND_TIMEOUT = 30;

    /**
     * 最大任务数
     */
    final static int MAX_TASK_SIZE = 0xfffff;

    /**
     * 序列化组件个数
     */
    final int SERIALIZER_SIZE = 5;

    /**
     * 将秒转换为毫秒的常量
     */
    final int SECONDS_HOLDER = 1000;

    final int SO_BLOCKING = 0xffff;
}
