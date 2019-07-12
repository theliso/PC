package test.ex4;

import main.ex4.SimpleThreadPoolExecutor;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Logger;

public class SimpleThreadPoolExecuterTest {

    private final int MAX_THREADPOOL_SIZE = 50;
    private final int KEEP_ALIVE_TIME = 1000; // in milliseconds
    private final Logger log = Logger.getLogger("test.ex4.SimpleThreadPoolExecuterTest");

    @Test
    public void shouldTestThreadPoolExecuteMethod() {
        SimpleThreadPoolExecutor threadPoolExecutor = new SimpleThreadPoolExecutor(MAX_THREADPOOL_SIZE, KEEP_ALIVE_TIME);
        List<Runnable> works = setup();
        works.forEach(runnable -> {
            try {
                boolean execute = threadPoolExecutor.execute(runnable, KEEP_ALIVE_TIME);
                log.info("execute result: " + execute);
            } catch (InterruptedException e) {
                log.warning("execute method error: " + e.getLocalizedMessage());
            }
        });
    }

    @Test
    public void shouldTestThreadPoolExecuteAwaitMethod() {
        SimpleThreadPoolExecutor threadPoolExecutor = new SimpleThreadPoolExecutor(MAX_THREADPOOL_SIZE, KEEP_ALIVE_TIME);
        List<Runnable> works = setup();
        final int[] idx = {0};
        works.forEach(runnable -> {
            try {
                ++idx[0];
                threadPoolExecutor.execute(runnable, KEEP_ALIVE_TIME);
                if (idx[0] == (works.size() / 2)) {
                    threadPoolExecutor.shutdown();
                    boolean await = threadPoolExecutor.awaitTermination(1000);
                    log.info("await result: " + await);
                }
            } catch (InterruptedException | RejectedExecutionException e){
                log.warning("execute method error: " + e.getLocalizedMessage());
            }
        });
    }

    private List<Runnable> setup() {
        List<Runnable> works = new LinkedList<>();
        for (int i = 0; i < 10000; i++) {
            works.add(() -> System.out.println("running work"));
        }
        return works;
    }

}
