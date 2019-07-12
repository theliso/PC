package test.ex2;


import main.ex2.Exchanger;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class ExchangerTest {

    private final int NUMBER_OF_THREADS = 20;
    private final List<String> toChange = new ArrayList<>(NUMBER_OF_THREADS);
    private final List<String> toCmp = new ArrayList<>(NUMBER_OF_THREADS);
    private final ArrayList<Thread> threads = new ArrayList<>(NUMBER_OF_THREADS);

    @Before
    public void setList(){
        toChange.add("a");
        toChange.add("b");
        toChange.add("c");
        toChange.add("d");
        toChange.add("e");
        toChange.add("f");
        toChange.add("g");
        toChange.add("h");
        toChange.add("i");
        toChange.add("j");
        toChange.add("k");
        toChange.add("l");
        toChange.add("m");
        toChange.add("n");
        toChange.add("o");
        toChange.add("p");
        toChange.add("q");
        toChange.add("r");
        toChange.add("s");
        toChange.add("t");
    }

    public void joinUninterruptibly(Thread jt) {
        do {
            try {
                jt.join();
                break;
            } catch (InterruptedException ie) {}
        } while (true);
    }


    @Test
    public void testExchangeBetweenTwoThreads() {
        Exchanger<String> exchanger = new Exchanger<>();
        final int[] idx = {0};
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            threads.add(i, new Thread(() -> {
                try {
                    int threadMsgIdx = idx[0]++;
                    Optional<String> exchange = exchanger.exchange(toChange.get(threadMsgIdx), 1000);
                    exchange.ifPresent(s ->
                            System.out.println("inside ifPresent threadMsgIdx = " + threadMsgIdx + " result = " + s));

                } catch (InterruptedException e) {
                }
            }));
            threads.get(i).start();
        }

        for (Thread thread : threads) {
            joinUninterruptibly(thread);
        }
    }

}
