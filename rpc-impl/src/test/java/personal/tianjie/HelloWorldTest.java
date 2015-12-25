package personal.tianjie;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import personal.tianjie.rpc.Constants;
import personal.tianjie.rpc.Response;
import personal.tianjie.rpc.client.NcfClient;
import personal.tianjie.rpc.client.NcfRPCFuture;
import personal.tianjie.rpc.exception.ServiceNotFoundException;

/**
 * Created by tianjie on 15-11-23.
 */
public class HelloWorldTest extends NcfAbstractCaseBase {
    @Test
    public void testSyncSayHello() throws ServiceNotFoundException {
        HelloWorldService helloWorldService = NcfClient.getSyncService(HelloWorldService.class);
        String str = helloWorldService.sayHello("tianjie");
        Assert.assertEquals("helloworld,tianjie", str);
    }

    @Test
    public void testASyncSayHello() throws ServiceNotFoundException, InterruptedException {
        HelloWorldService helloWorldService = NcfClient.getASyncService(HelloWorldService.class);
        NcfRPCFuture future = helloWorldService.sayHello("tianjie");
        Response response = (Response) future.get();
        Assert.assertTrue(response.getStatus() == Constants.Status.OK);
        Assert.assertEquals("helloworld,tianjie", response.getResult());
    }

    @AfterClass
    public static void after(){
        LOGGER.info("测试完毕.");
    }
}
