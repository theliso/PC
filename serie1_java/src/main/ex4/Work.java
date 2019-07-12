package main.ex4;

import java.util.concurrent.locks.Condition;

public class Work {

    public Condition delivered;
    public Runnable work;

    public Work(Condition delivered, Runnable work) {
        this.delivered = delivered;
        this.work = work;
    }
}
