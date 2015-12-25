package personal.tianjie.rpc.client;

/**
 * RPC服务器地址.
 * Created by tianjie on 7/14/15.
 */
public class NcfAddress {

    /**
     *
     * 地址.
     */
    private String host;

    /**
     *
     * 端口.
     */
    private Integer port;

    /**
     *
     * 构造函数.
     *
     * @param aHost 地址
     * @param aPort 端口
     */
    public NcfAddress(final String aHost,
                      final Integer aPort) {
        this.host = aHost;
        this.port = aPort;
    }

    /**
     * 获取地址.
     *
     * @return 地址
     */
    public String getHost() {
        return host;
    }

    /**
     * 获取端口.
     *
     * @return 端口
     */
    public Integer getPort() {
        return port;
    }
}
