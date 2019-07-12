package main.ex4;

import main.TimeoutHolder;

import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class WorkerThreads extends Thread {


    private Lock mLock;
    private TimeoutHolder th;
    private Queue<Runnable> work;
    public Condition delivered;
    private int keepAliveTime;
    private final Consumer<Condition> consumer;
    private final Logger log = Logger.getLogger("main.ex4.WorkerThreads");

    public WorkerThreads(Queue<Runnable> work, int keepAliveTime, Condition condition, Consumer<Condition> consumer) {
        this.work = work;
        this.delivered = condition;
        th = new TimeoutHolder(keepAliveTime);
        this.keepAliveTime = keepAliveTime;
        this.consumer = consumer;
        mLock = new ReentrantLock();
    }

    @Override
    public void run() {
        while (true) {
            if (!work.isEmpty() && th.value() != 0){
                Runnable remove;
                //mLock.lock();
                //try {
                    remove = work.remove();
                //} finally {
                  //  mLock.unlock();
                //}
                consumer.accept(delivered);
                remove.run();
                th = new TimeoutHolder(keepAliveTime);
            } else {
                if (th.value() == 0)
                    break;
            }
        }
    }
}
