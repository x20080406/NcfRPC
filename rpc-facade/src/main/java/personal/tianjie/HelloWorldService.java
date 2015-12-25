package personal.tianjie;


import personal.tianjie.rpc.RpcService;

/**
 * Created by tianjie on 7/19/15.
 */
@RpcService
public interface HelloWorldService {
    /**
     * 发送helloworld信息
     * @param name 姓名
     * @return helloworld信息
     */
    <T> T sayHello(String name);
    /**
     * 发送helloworld信息
     * @param name 姓名
     * @param data
     * @return helloworld信息
     */
    <T> T sayHello(String name,byte[] data);
}
