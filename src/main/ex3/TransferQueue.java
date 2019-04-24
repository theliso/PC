package main.ex3;

import main.TimeoutHolder;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TransferQueue<T> {

    private final Lock mLock = new ReentrantLock();

    private Queue<Messages<T>> msgQueue = new LinkedList<>();

    /**
     * deliver/add a message to the list and never blocks the current thread
     *
     * @param msg -> The message to be put in the list
     */
    public void put(T msg) {
        msgQueue.add(new Messages<>(msg, mLock.newCondition()));
    }

    /**
     * add the message to the list and waits to be delivered to other thread through take method
     *
     * @param msg     -> msg to be delivered
     * @param timeout -> timeout for the operation
     * @return true if the message was successfully received by other thread;
     * false if achieves timeout and the other thread doesn't get the message
     * @throws InterruptedException when the waiting thread gets interrupted
     *                              When this method throws the exception the message must be removed from the list
     */
    public boolean transfer(T msg, int timeout) throws InterruptedException {
        mLock.lock();
        try {
            Condition condition = mLock.newCondition();
            Messages<T> msgTransfer = new Messages<>(msg, condition);
            msgQueue.add(msgTransfer);
            TimeoutHolder th = new TimeoutHolder(timeout);
            do {
                if (th.value() == 0) {
                    msgQueue.remove();
                    return false;
                }
                try {
                    condition.wait(th.value());
                } catch (InterruptedException e) {
                    if (msgTransfer.delivered) {
                        Thread.currentThread().interrupt();
                        return true;
                    }
                    if (msgTransfer.interrupted) {
                        msgQueue.remove();
                        return false;
                    }
                }
            } while (!msgTransfer.delivered);
            return true;
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Take the message from the list
     *
     * @param timeout -> The time for the taken operation be successful
     * @return true if it returns a message whithin the tomeout stablished
     * false if timeout reaches and the thread waiting gets interrupted
     */
    public T take(int timeout) throws InterruptedException {
        if (msgQueue.size() == 0) {
            return null;
        }
        TimeoutHolder th = new TimeoutHolder(timeout);
        if (th.value() != 0) {
            Messages<T> msg = msgQueue.remove();
            if (msg.interrupted) {
                msgQueue.add(msg);
                throw new InterruptedException();
            }
            msg.delivered = true;
            msg.condition.notify();
            return msg.msg;
        }
        return null;
    }
}
