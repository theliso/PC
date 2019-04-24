package main.ex4;

public class WorkerThreads {

    public int keepAliveTime;
    public Thread workerThread;
    public boolean finishedWork;
    public Work cmd;


    public WorkerThreads(int keepAliveTime, Thread workerThread, boolean finishedWork, Work cmd) {
        this.keepAliveTime = keepAliveTime;
        this.workerThread = workerThread;
        this.finishedWork = finishedWork;
        this.cmd = cmd;
    }
}
