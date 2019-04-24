package main;

public class Request<T> {

    public boolean done;
    public T dataToTrade;
    public T dataToKeep;

    public Request(T dataToTrade, T dataToKeep) {
        this.dataToTrade = dataToTrade;
        this.dataToKeep = dataToKeep;
        done = false;

    }
}
