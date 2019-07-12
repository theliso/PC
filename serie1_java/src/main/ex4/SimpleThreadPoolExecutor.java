package main.ex4;

import main.TimeoutHolder;

import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class SimpleThreadPoolExecutor {

    private final Condition allWorkFinishCondition;
    private Lock mLock;
    private boolean isShuttingDown = false;
    private boolean done = false;
    private int maxPoolSize;
    private int keepAliveTime;

    private int numberOfWorkers = 0;
    private Logger log = Logger.getLogger("main.ex4.SimpleThreadPoolExecutor");

    private Queue<WorkerThreads> threadPool;
    private final Queue<Work> work = new LinkedList<>();


    private boolean shutdownConcluded = false;

    /**
     * @param maxPoolSize   -> max number of worker threads in the pool
     * @param keepAliveTime -> max time before they can be destroyed
     */
    public SimpleThreadPoolExecutor(int maxPoolSize, int keepAliveTime) {
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        threadPool = new LinkedList<>();
        mLock = new ReentrantLock();
        allWorkFinishCondition = mLock.newCondition();
    }


    /**
     * @param command
     * @param timeout
     * @return
     * @throws InterruptedException
     */
    public boolean execute(Runnable command, int timeout) throws InterruptedException, RejectedExecutionException {
        mLock.lock();
        try {
            if (isShuttingDown) {
                throw new RejectedExecutionException();
            }
            Work request = new Work(mLock.newCondition(), command);
            work.add(request);
            if (threadPool.size() < maxPoolSize) {
                WorkerThreads workerThreads = new WorkerThreads(
                        work,
                        threadPool,
                        keepAliveTime,
                        mLock.newCondition(),
                        mLock,
                        shutdownConcluded,
                        allWorkFinishCondition,
                        done
                );
                threadPool.add(workerThreads);
                workerThreads.start();
                ++numberOfWorkers;
                log.warning("workers: " + numberOfWorkers);
            } else {
                threadPool.peek().awaitWakeUp.signal();
            }
            TimeoutHolder th = new TimeoutHolder(timeout);
            do {
                if (th.value() == 0) {
                    work.remove(request);
                    break;
                }
                try {
                    request.delivered.await(th.value(), TimeUnit.MILLISECONDS);
                    if (th.value() != 0) {
                        return true;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            } while (true);
            return false;
        } finally {
            mLock.unlock();
        }

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
            if(threadPool.isEmpty()){
                return true;
            }
            TimeoutHolder th = new TimeoutHolder(timeout);
            do {
                if (th.value() == 0) {
                    return false;
                }
                try {
                    allWorkFinishCondition.await(th.value(), TimeUnit.MILLISECONDS);
                } catch (InterruptedException e){
                    if (done){
                        Thread.currentThread().interrupt();
                        return true;
                    }
                    throw e;
                }
            } while (!done);
            return true;
        } finally {
            mLock.unlock();
        }
    }
}
