package main.ex4;

import main.TimeoutHolder;

import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleThreadPoolExecutor {

    private Lock mLock;
    private boolean isShuttingDown = false;
    private int maxPoolSize;
    private int keepAliveTime;
    private Condition threadPoolFull;
    private Condition hasTerminate;

    private Queue<WorkerThreads> threadPool;


    private boolean shutdownConcluded = false;

    /**
     * @param maxPoolSize   -> max number of worker threads in the pool
     * @param keepAliveTime -> max time before they can be destroyed
     */
    public SimpleThreadPoolExecutor(int maxPoolSize, int keepAliveTime) {
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        threadPool = new PriorityQueue<>();
        mLock = new ReentrantLock();
        threadPoolFull = mLock.newCondition();
        hasTerminate = mLock.newCondition();
    }


    public boolean execute(Runnable command, int timeout) throws InterruptedException {
        mLock.lock();
        try {
            TimeoutHolder th = new TimeoutHolder(timeout);
            do {
                if (isShuttingDown) {
                    throw new RejectedExecutionException();
                }
                if (threadPool.size() == maxPoolSize) {
                    try {
                        threadPoolFull.wait();
                    } catch (InterruptedException e) {
                        throw new InterruptedException();
                    }
                }
                if (threadPool.size() == 0) {
                    WorkerThreads workerThreads =
                            new WorkerThreads(
                                    new TimeoutHolder(keepAliveTime),
                                    command,
                                    threadPoolFull,
                                    threadPool,
                                    hasTerminate
                            );
                    workerThreads.start();
                    return true;
                }
                WorkerThreads workerThreads = threadPool.remove();
                if (workerThreads.keepAliveTime.value() > 0) {
                    workerThreads.keepAliveTime = new TimeoutHolder(keepAliveTime);
                    workerThreads.cmd = command;
                    workerThreads.start();
                    return true;
                }
            } while (th.value() > 0);
        } finally {
            mLock.unlock();
        }
        return false;
    }


    public void shutdown() {
        mLock.lock();
        try {
            isShuttingDown = true;
        } finally {
            mLock.unlock();
        }
    }


    public boolean awaitTermination(int timeout) throws InterruptedException {
        mLock.lock();
        try {
            TimeoutHolder th = new TimeoutHolder(timeout);
            int amountOfWork = 0;
            do {
                try{
                    hasTerminate.wait();
                    ++amountOfWork;
                } catch (InterruptedException e){
                    throw new InterruptedException();
                }
                if(th.value() == 0){
                    return false;
                }
            }while (amountOfWork < threadPool.size());
        }finally {
            mLock.unlock();
        }
        return true;
    }

}
