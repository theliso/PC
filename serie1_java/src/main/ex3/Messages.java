package main.ex3;

import java.util.concurrent.locks.Condition;

public class Messages<T> {

    public T msg;
    public Condition condition;
    public boolean delivered = false;
    public boolean interrupted = false;

    public Messages(T msg, Condition condition) {
        this.msg = msg;
        this.condition = condition;
    }
}
