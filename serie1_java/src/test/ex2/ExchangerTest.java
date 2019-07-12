package test.ex2;


import main.ex2.Exchanger;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ExchangerTest {

    private final int NUMBER_OF_THREADS = 20;
    private final List<String> toChange = new ArrayList<>(NUMBER_OF_THREADS);
    private final ArrayList<Thread> threads = new ArrayList<>(NUMBER_OF_THREADS);

    @Before
    private void setList(){
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

    @Test
    public void testExchange() {
        toChange.forEach(System.out::println);
        List<String> toCompare = toChange;
        Exchanger<String> exchanger = new Exchanger<>();
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            final int[] idx = {i};
            threads.add(i, new Thread(() -> {
                try {
                    exchanger.exchange(toChange.get(idx[0]), 1000);
                } catch (InterruptedException e) {
                }
            }));
        }
        for (int i = 0, j = toCompare.size() - 1; i < toCompare.size(); i++) {
            assertEquals(toChange.get(j), toCompare.get(i));
        }

    }

}
