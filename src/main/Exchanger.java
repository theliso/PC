package main;

import java.util.Optional;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Exchanger<T> {

    private final Lock mLock;
    private Request<T> exchangeholder = null;

    public Exchanger() {
        mLock = new ReentrantLock();
    }

    public Optional<T> exchange(T myData, long timeout) throws InterruptedException {
        mLock.lock();
        try {
            Condition condition = mLock.newCondition();
            if (exchangeholder == null) {
                exchangeholder = new Request<>(myData, null);
            } else {
                exchangeholder.dataToKeep = myData;
                exchangeholder.done = true;
                condition.notify();
                return Optional.of(exchangeholder.dataToTrade);
            }
            TimeoutHolder th = new TimeoutHolder(timeout);
            do {
                if ((timeout = th.value()) == 0){
                    return Optional.empty();
                }
                try {
                    condition.wait(timeout);
                }catch (InterruptedException interrupted){
                    if (exchangeholder.done){
                        Thread.currentThread().interrupt();
                        return Optional.of(exchangeholder.dataToKeep);
                    }
                    throw new InterruptedException();
                }
            } while (!exchangeholder.done);
            return Optional.of(exchangeholder.dataToKeep);
        } finally {
            mLock.unlock();
        }
    }

}
