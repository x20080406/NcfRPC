import com.google.common.collect.Lists;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import personal.tianjie.HelloWorldService;
import personal.tianjie.rpc.client.NcfAddress;
import personal.tianjie.rpc.client.NcfClient;
import personal.tianjie.rpc.client.NcfConnectionManager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by tianjie on 7/20/15.
 */
public class BenchmarkTestCase  {
    private static Logger logger = LoggerFactory.getLogger(BenchmarkTestCase  .class);
    @Test
    public  void benchmarkTest() throws Exception {
        NcfConnectionManager.getInstance().start(Lists.newArrayList(
                new NcfAddress("127.0.0.1", 23432)));

        final int limitTime = 60000,
                cpuSize = Runtime.getRuntime().availableProcessors(),
                threadCount = cpuSize * 8;
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        final CyclicBarrier cyclicBarrier = new CyclicBarrier(threadCount);
        final long startTime = System.currentTimeMillis() + 1000;
        final long endTime = startTime + limitTime;
        final AtomicLong successTransactions = new AtomicLong(0);
        final AtomicLong errorTransactions = new AtomicLong(0);

        for (int i = 0; i < threadCount; i++)
            new Thread(
                    new BenchmarkRunnable(
                            countDownLatch, cyclicBarrier,
                            startTime, endTime,
                            successTransactions, errorTransactions)
            ).start();
        countDownLatch.await();

        logger.info("success:" + successTransactions.get() + "-" +
                (successTransactions.get() / (limitTime / 1000))
                + "\nerror:" + errorTransactions.get() + "-" +
                (errorTransactions.get() / (limitTime / 1000)));
        NcfConnectionManager.getInstance().shutdown();
    }
}

class BenchmarkRunnable implements Runnable {
    private CountDownLatch countDownLatch;
    private CyclicBarrier cyclicBarrier;
    private long endTime, startTime;
    private AtomicBoolean running = new AtomicBoolean(true);
    private AtomicLong successTransactions;
    private AtomicLong errorTransactions;

    public BenchmarkRunnable(CountDownLatch countDownLatch,
                             CyclicBarrier cyclicBarrier,
                             long startTime, long endTime,
                             AtomicLong successTransactions,
                             AtomicLong errorTransactions) {
        this.countDownLatch = countDownLatch;
        this.cyclicBarrier = cyclicBarrier;
        this.startTime = startTime;
        this.endTime = endTime;
        this.successTransactions = successTransactions;
        this.errorTransactions = errorTransactions;
    }

    public void run() {
        try {
            cyclicBarrier.await();
        } catch (Exception e) {
            // IGNORE
        }

        benchmarkTest();

        countDownLatch.countDown();
    }

    private void benchmarkTest() {
        while (running.get()) {
            long beginTime = System.currentTimeMillis();
            if (beginTime >= endTime) {
                running.compareAndSet(true, false);
                break;
            }
            if (beginTime <= startTime) {//所有线程都已准备就绪，但未到达开始时间
                continue;
            }
            try {
                HelloWorldService helloWorldService = NcfClient.getSyncService(HelloWorldService.class,
                        HelloWorldService.class.getPackage().getName());
                String rs = helloWorldService.sayHello(Thread.currentThread().getName(),
                        Thread.currentThread().getName().getBytes());
                if (rs != null)
                    successTransactions.incrementAndGet();
            } catch (Exception e) {
                errorTransactions.incrementAndGet();
            }
        }
    }
}