package personal.tianjie;
/*

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.Transaction;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;
*/

/**
 * 常量定义
 */
/*class Const {
    final static JedisPool jedisPool = new JedisPool(
            new GenericObjectPoolConfig(),
            "127.0.0.1", 6379,
            Protocol.DEFAULT_TIMEOUT, null, 10, null);

    final static String key = "INCR_KEY";

    final static int RANGE = 100,              //随机数范围
            MAX_SIZE = 1001,                     //参与自增的数组
            MAX_VAL =5000;// MAX_SIZE * RANGE / 2;
                                                // 在REDIS中，最多增加到MAX_VAL
}*/

public class RedisCASOpTest {
    /*public static void main(String... args) {
        Integer[] arr = new Integer[Const.MAX_SIZE];
        Random random = new Random();

        for (int i = 0; i < Const.MAX_SIZE; i++) {
            arr[i] = Math.abs(random.nextInt(Const.RANGE));
        }

        ForkJoinPool.commonPool().invoke(new IncrTask(arr, 0, arr.length));
    }*/
}
/*
class IncrTask extends RecursiveAction {
    private String name="parent";
    static AtomicInteger idx = new AtomicInteger();
    static final int COMPUTE_THRESHOLD = 30;

    int low;
    int high;
    Integer[] array;

    IncrTask(Integer[] arr, int lo, int hi) {
        array = arr;
        low = lo;
        high = hi;
    }

    protected void compute() {
        if (high - low <= COMPUTE_THRESHOLD) {
            for (int i = low; i < high; ++i) {
                try (Jedis jedis = Const.jedisPool.getResource()) {
                    boolean flag = true;
                    int retry = 3;//重试次数
                    do {
                        //http://redis.io/commands/exec
                        // When using WATCH, EXEC will execute commands only if the watched keys were not modified.
                        // EXEC can return a Null reply if the execution was aborted.
                        jedis.watch(Const.key.getBytes());

                        byte[] d = jedis.get(Const.key.getBytes());
                        long val = 0;

                        if (d != null) {
                            val = Long.valueOf(new String(d));
                        }

                        if ((val + array[i]) < Const.MAX_VAL) {
                            Transaction tx = jedis.multi();
                            tx.incrBy(Const.key.getBytes(), array[i]);
                            tx.expire(Const.key.getBytes(), 1000);
                            List<Object> objects = tx.exec();
                            flag = objects != null && objects.get(0) != null;
                        } else {
                            jedis.unwatch();
                        }

                    } while (!flag && --retry > 0);
                }
            }
        } else {
            System.out.println(name+"->"+idx.getAndIncrement()+":"+high+"-"+low);
            int mid = low + (high - low) / 2;
            IncrTask left = new IncrTask(array, low, mid);
            left.name="left";
            IncrTask right = new IncrTask(array, mid, high);
            right.name="right";
            left.fork();        //left调用fork后，会将left加入到执行队列中等待ForkJoinPool.common线程组执行。ForkJoinPool.common是公共的，不用像ExecutorService到处去构造ExecutorService来执行任务
            right.compute();    //当前线程继续执行 right,
            left.join();        //block,until left complete

            *//**                     o
            *                    /    \
            *                   o      o
            *                  /  \   / \
            *                 o   o  o   o
            *//*

        }
    }
}*/
