package main.ex2;

import main.TimeoutHolder;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Exchanger<T> {

    private final Lock mLock;
    private final List<Request<T>> waiters = new LinkedList<>();

    public Exchanger() {
        mLock = new ReentrantLock();
    }

    public Optional<T> exchange(T myData, long timeout) throws InterruptedException {
        mLock.lock();
        try {
            if (waiters.size() > 0) {
                Request<T> tRequest = waiters.remove(0);
                tRequest.dataToKeep = myData;
                tRequest.done = true;
                tRequest.condition.signal();
                return Optional.of(tRequest.dataToTrade);
            }
            Request<T> req = new Request<>(myData, null, mLock.newCondition());
            TimeoutHolder th = new TimeoutHolder(timeout);
            do {
                if ((timeout = th.value()) == 0){
                    return Optional.empty();
                }
                try{
                    waiters.add(req);
                    req.condition.await(timeout, TimeUnit.MILLISECONDS);
                }catch (InterruptedException interrupted){
                    if (req.done){
                        Thread.currentThread().interrupt();
                        waiters.remove(0);
                        return Optional.of(req.dataToKeep);
                    }
                }
            } while (!req.done);
            return Optional.of(req.dataToKeep);
        } finally {
            mLock.unlock();
        }
    }

}
