package main.ex4;

import main.TimeoutHolder;

import java.util.Queue;
import java.util.concurrent.locks.Condition;

public class WorkerThreads extends Thread {

    public TimeoutHolder keepAliveTime;
    public Runnable cmd;
    private Condition threadPoolFull;
    private Queue<WorkerThreads> threadPool;
    private Condition hasTerminate;


    public WorkerThreads(
            TimeoutHolder keepAliveTime,
            Runnable cmd,
            Condition threadPoolFull,
            Queue<WorkerThreads> threadPool,
            Condition hasTerminate
    ) {
        this.keepAliveTime = keepAliveTime;
        this.cmd = cmd;
        this.threadPoolFull = threadPoolFull;
        this.threadPool = threadPool;
        this.hasTerminate = hasTerminate;
    }

    @Override
    public void run() {
        try{
           cmd.run();
        }finally {
            notifyWorkerThreadEnd();
        }
    }

    private void notifyWorkerThreadEnd() {
        threadPool.add(this);
        threadPoolFull.notify();
        hasTerminate.notify();
    }
}
