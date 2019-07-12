package main.ex2;

import java.util.concurrent.locks.Condition;

public class Request<T> {

    public boolean done;
    public T dataToTrade;
    public T dataToKeep;
    public Condition condition;

    public Request(T dataToTrade, T dataToKeep, Condition condition) {
        this.dataToTrade = dataToTrade;
        this.dataToKeep = dataToKeep;
        this.condition = condition;
        done = false;

    }
}
