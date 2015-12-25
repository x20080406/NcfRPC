package personal.tianjie.rpc.server;

import personal.tianjie.rpc.Constants;

/**
 * Created by tianjie on 4/23/15.
 */
public class NcfServerConfig {
    private int port = Constants.SERVER_PORT;
    private int evtExecutorSize = Constants.EVT_EXECUTOR_SIZE;
    private int bossExecutorSize = Constants.BOSS_EXECUTOR_SIZE;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getEvtExecutorSize() {
        return evtExecutorSize;
    }

    public void setEvtExecutorSize(int evtExecutorSize) {
        this.evtExecutorSize = evtExecutorSize;
    }

    public int getBossExecutorSize() {
        return bossExecutorSize;
    }

    public void setBossExecutorSize(int bossExecutorSize) {
        this.bossExecutorSize = bossExecutorSize;
    }
}
