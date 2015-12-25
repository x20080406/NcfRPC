import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * Created by tianjie on 7/16/15.
 */
public class Test {
    static Random random = new Random();
    final static int max = 999999;
    final static int min = 111111;

    /**
     * 生成一个大于等于111111小于999999区间的整数
     *
     * @return
     */
    public static Integer randomNumber() {
        return (int) (Math.random() * (max - min)) + min;
    }

    public static void main(String... args) throws Exception {
        int total = 20000000;
        long start1 = System.currentTimeMillis();
        List l1 = multiThread(total);
        long start2 = System.currentTimeMillis();
        List l2 = singleThread(total);
        long end = System.currentTimeMillis();
        System.out.println("L1:" + (start2 - start1));
        System.out.println("L2:" + (end - start2));
    }

    static List<Integer> singleThread(int totalCount) throws Exception {
        Integer[] arr = new Integer[totalCount];
        List l = new ArrayList();
        for(int i=0;i<totalCount;i++){
//            arr[i]=randomNumber();
            l.add(randomNumber());
        }
//        return Arrays.asList(arr);
        return l;
    }

    static List<Integer> multiThread(int totalCount) throws Exception {
        final int processorCount = Runtime.getRuntime().availableProcessors(),
                loopCount = totalCount / processorCount;
        final Integer[] arr = new Integer[totalCount];
        final CountDownLatch cdl = new CountDownLatch(processorCount);

        for (int i = 0; i < processorCount; i++) {
            final int j = i * loopCount;
            new Thread() {
                public void run() {
                    for (int i1 = 0; i1 < loopCount; i1++) {
                        arr[j + i1] = randomNumber();
                    }
                    cdl.countDown();
                }
            }.start();
        }
        cdl.await();
        for(int i=totalCount-1;i>=totalCount * processorCount;i--)
            arr[i]=randomNumber();
        return Arrays.asList(arr);
    }
}
