package test.ex3;

import main.ex3.TransferQueue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;

public class TransferQueueTest {


    private final Logger log = Logger.getLogger("test.ex3.TransferQueueTest");

    private void joinUninterruptibly(Thread jt) {
        do {
            try {
                jt.join();
                break;
            } catch (InterruptedException ie) {}
        } while (true);
    }

    @Test
    public void testMessagePutAndTakenBetweenThreads() throws InterruptedException {
        final int NUMBER_OF_THREADS = 100;
        List<Thread> threads = new ArrayList<>(NUMBER_OF_THREADS);
        TransferQueue<String> sync = new TransferQueue<>();
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            final int[] nr = {i};
            threads.add(i, new Thread(() -> sync.put("msg: " + nr[0])));
            threads.get(i).start();
        }

        for (Thread thread : threads) {
            joinUninterruptibly(thread);
        }
        take(NUMBER_OF_THREADS, threads, sync);
    }

    @Test
    public void testMessageTransferBetweenThreads() throws InterruptedException {
        final int NUMBER_OF_THREADS = 100;
        List<Thread> threadsTransfer = new ArrayList<>(NUMBER_OF_THREADS);
        TransferQueue<String> sync = new TransferQueue<>();
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            final int[] nr = {i};
            threadsTransfer.add(new Thread(() -> {
                try {
                    sync.transfer("msg: " + nr[0], 1000);
                } catch (InterruptedException e) {
                    log.warning("transfer Error: " + e.getLocalizedMessage());
                }
            }));
        }

        threadsTransfer.forEach(Thread::start);
        Thread.sleep(500);
        List<Thread> threadsTake = new ArrayList<>(NUMBER_OF_THREADS);

        List<String> msgs = new ArrayList<>(NUMBER_OF_THREADS);
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            threadsTake.add(new Thread(() -> {
                try {
                    msgs.add(sync.take(1000));
                } catch (InterruptedException e) {
                    log.warning("Take Error: " + e.getLocalizedMessage());
                }
            }));
        }


        threadsTake.forEach(Thread::start);
        for (Thread thread : threadsTake) {
            joinUninterruptibly(thread);
        }
        for (Thread thread : threadsTransfer) {
            joinUninterruptibly(thread);
        }

        msgs.forEach(System.out::println);

    }

    private void take(int NUMBER_OF_THREADS, List<Thread> threads, TransferQueue<String> sync) {

        List<String> msgs = new ArrayList<>(NUMBER_OF_THREADS);
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            threads.add(i, new Thread(() -> {
                try {
                    msgs.add(sync.take(1000));
                } catch (InterruptedException ignored) {
                }
            }));
            threads.get(i).start();
        }


        for (Thread thread : threads) {
            joinUninterruptibly(thread);
        }

        assertEquals(NUMBER_OF_THREADS, msgs.size());
    }
}
