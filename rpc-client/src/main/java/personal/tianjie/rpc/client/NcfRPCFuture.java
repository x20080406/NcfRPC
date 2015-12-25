package personal.tianjie.rpc.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by tianjie on 4/12/15.
 */
public class NcfRPCFuture<T> implements Future<T> {
    private volatile T response;

    private volatile Boolean cancelled = Boolean.FALSE;

    private final CountDownLatch responseLatch;

    private final NcfConnectionManager.TaskManager taskManager;

    public NcfRPCFuture(Long id, NcfConnectionManager.TaskManager taskManager) {
        this.id = id;
        this.responseLatch = new CountDownLatch(1);
        this.taskManager = taskManager;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        if (this.isDone()) {
            return Boolean.FALSE;

        } else {
            try {
                this.responseLatch.countDown();
                this.cancelled = Boolean.TRUE;
                return !this.isDone();
            }finally{
                this.taskManager.removeTask(this.getId());
            }
        }
    }

    private final Long id;

    public Long getId() {
        return this.id;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public boolean isDone() {
        return this.responseLatch.getCount() == 0;
    }

    public T get() throws InterruptedException {
        try {
            this.responseLatch.await();
        } catch (InterruptedException e) {
            throw e;
        }finally{
            this.taskManager.removeTask(this.getId());
        }

        return this.response;
    }

    public T get(long timeout, TimeUnit unit) throws InterruptedException {
        try {
            this.responseLatch.await(timeout, unit);
        } catch (InterruptedException e) {
            throw e;
        }finally {
            this.taskManager.removeTask(this.getId());
        }

        return this.response;
    }

    public void commit(T response) {
        this.response = response;
        this.responseLatch.countDown();

        this.taskManager.removeTask(this.getId());
    }

}
