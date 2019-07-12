package test.ex2;

import main.ex2.UnsafeRefCountedHolder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class UnsafeRefCountedHolderTest {

    private final int NUMBER_OF_THREADS = 100;
    private final Logger _log = Logger.getLogger("UnsafeRefCountedHolderTest");

    @Test
    public void shouldTestAddRefAndReleaseRefMethod() throws InterruptedException {
        UnsafeRefCountedHolder<String> ref = new UnsafeRefCountedHolder<>("Hello World");
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            threads.add(new Thread(() -> {
                ref.addRef();
                try {
                    Thread.sleep(1000);
                    System.out.println(ref.getValue());
                    ref.ReleaseRef();
                } catch (Exception e) {
                    _log.warning("exception: " + e.getLocalizedMessage());
                }

            }));
            threads.get(i).start();
        }
        for (int i = 0; i < threads.size(); i++) {
            threads.get(i).join();
        }
        Thread t = new Thread(() -> {
            try {
                System.out.println(ref.getValue());
            } catch (Exception e) {
                _log.warning("exception: " + e.getLocalizedMessage());
            }
        });
        t.start();
        t.join();
    }

}
