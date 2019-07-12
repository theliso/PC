package main.ex3;

import main.TimeoutHolder;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class TransferQueue<T> {

    private final Lock mLock;

    private Queue<Messages<T>> msgQueue = new LinkedList<>();
    private final Logger logger = Logger.getLogger("main.ex3.TranferQueue");

    public TransferQueue() {
        mLock = new ReentrantLock();
    }

    /**
     * deliver/add a message to the list and never blocks the current thread
     *
     * @param msg -> The message to be put in the list
     */
    public void put(T msg) {
        mLock.lock();
        try {
            msgQueue.add(new Messages<>(msg, mLock.newCondition()));
        } finally {
            mLock.unlock();
        }
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
    public boolean transfer(T msg, long timeout) throws InterruptedException {
        mLock.lock();
        try {
            logger.info("transfer executing: " + msg.toString());
            Messages<T> msgTransfer = new Messages<>(msg, mLock.newCondition());
            put(msg);
            TimeoutHolder th = new TimeoutHolder(timeout);
            do {
                if ((timeout = th.value()) == 0) {
                    msgQueue.remove();
                    return false;
                }
                try {
                    msgTransfer.condition.await(timeout, TimeUnit.MILLISECONDS);
                    msgTransfer = msgQueue.remove();
                } catch (InterruptedException e) {
                    if (msgTransfer.delivered) {
                        msgQueue.remove();
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                    msgTransfer.interrupted = true;
                    return false;
                }
            } while (!msgTransfer.delivered);
            logger.info("transfer executed successfully");
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
    public T take(long timeout) throws InterruptedException {
        mLock.lock();
        try {
            logger.info("take executing");
            TimeoutHolder th = new TimeoutHolder(timeout);
            do {
                if ((timeout = th.value()) == 0) {
                    return null;
                }
                Messages<T> msg = msgQueue.remove();
                if (msg.interrupted){
                    logger.warning("Thread interrupted");
                    throw new InterruptedException();
                }
                msg.delivered = true;
                msg.condition.signal();
                msgQueue.add(msg);
                logger.info("Take executed successfully");
                return msg.msg;
            } while (msgQueue.size() > 0);
        } finally {
            mLock.unlock();
        }
    }

}
