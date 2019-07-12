package main.ex4;

import main.TimeoutHolder;

import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class WorkerThreads extends Thread {


    private final Lock mLock;
    private TimeoutHolder th;
    private final Queue<Work> work;
    private final Queue<WorkerThreads> threadPool;
    private final int keepAliveTime;
    public final Condition awaitWakeUp;
    private final boolean shuttingDown;
    private final Condition allWorkFinishCondition;
    private boolean done;
    private final Logger log = Logger.getLogger("main.ex4.WorkerThreads");

    public WorkerThreads(
            Queue<Work> work,
            Queue<WorkerThreads> threadPool,
            int keepAliveTime,
            Condition condition,
            Lock mLock,
            boolean shuttingDown,
            Condition allWorkFinishCondition,
            boolean done) {
        this.work = work;
        this.threadPool = threadPool;
        th = new TimeoutHolder(keepAliveTime);
        this.keepAliveTime = keepAliveTime;
        this.mLock = mLock;
        awaitWakeUp = condition;
        this.shuttingDown = shuttingDown;
        this.allWorkFinishCondition = allWorkFinishCondition;
        this.done = done;
    }

    @Override
    public void run() {
        mLock.lock();
        try {
            do {
                if (!work.isEmpty()) {
                    threadPool.remove(this);
                    Work remove = work.remove();
                    remove.work.run();
                    remove.delivered.signal();
                    threadPool.add(this);
                    th = new TimeoutHolder(keepAliveTime);
                }
                this.awaitWakeUp.await(th.value(), TimeUnit.MILLISECONDS);
            } while (th.value() != 0);
            if (shuttingDown && threadPool.isEmpty()) {
                allWorkFinishCondition.signalAll();
                done = true;
            }
            threadPool.remove(this);
        } catch (InterruptedException e) {
            log.warning("interrupted thread: " + e.getLocalizedMessage());
        } finally {
            mLock.unlock();
        }
    }
}