package main.ex4;

import main.TimeoutHolder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleThreadPoolExecutor {

    private Lock mLock;
    private Condition workDelivered;
    private Condition allWorkDone;
    private int maxPoolSize;
    private int keepAliveTime;
    private boolean isShuttingDown = false;
    private boolean shutdownConcluded = false;
    private List<WorkerThreads> threadPool;
    private List<Work> workQueue = new LinkedList<>();

    /**
     * @param maxPoolSize   -> max number of worker threads in the pool
     * @param keepAliveTime -> max time before they can be destroyed
     */
    public SimpleThreadPoolExecutor(int maxPoolSize, int keepAliveTime) {
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        threadPool = new ArrayList<>(maxPoolSize);
        mLock = new ReentrantLock();
        workDelivered = mLock.newCondition();
        allWorkDone = mLock.newCondition();
    }

    /**
     * @param command
     * @param timeout
     * @return
     * @throws InterruptedException
     */
    public boolean execute(Runnable command, int timeout) throws InterruptedException {
        mLock.lock();
        try {
            if (isShuttingDown) {
                throw new RejectedExecutionException();
            }

            return false;
        } finally {
            mLock.unlock();
        }
    }


    /**
     *
     */
    public void shutdown() {
        mLock.lock();
        try {
            isShuttingDown = true;
            Condition condition = mLock.newCondition();
            shutdownConcluded = true;
        } finally {
            mLock.unlock();
        }
    }

    /**
     * @param timeout
     * @return
     * @throws InterruptedException
     */
    public boolean awaitTermination(int timeout) throws InterruptedException {
        if (shutdownConcluded) {
            return true;
        }
        TimeoutHolder th = new TimeoutHolder(timeout);

        return false;
    }

}
