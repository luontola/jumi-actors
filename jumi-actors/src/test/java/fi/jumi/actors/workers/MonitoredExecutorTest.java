// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.workers;

import org.junit.*;

import java.util.concurrent.*;

import static org.mockito.Mockito.*;

public class MonitoredExecutorTest {

    private static final long TIMEOUT = 1000;

    private final UncaughtExceptionCollector uncaughtExceptions = new UncaughtExceptionCollector();
    private final Runnable callback = mock(Runnable.class);
    private final WorkerCounter workerCounter = new WorkerCounter(callback);
    private ExecutorService realExecutor;

    @Before
    public void startExecutor() {
        realExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setUncaughtExceptionHandler(uncaughtExceptions);
                return t;
            }
        });
    }

    @After
    public void stopExecutor() throws Throwable {
        realExecutor.shutdown();
        uncaughtExceptions.failIfNotEmpty();
    }


    @Test(timeout = TIMEOUT)
    public void when_all_commands_are_finished_then_the_callback_is_fired() throws InterruptedException {
        Executor executor = new MonitoredExecutor(realExecutor, workerCounter);

        CountDownLatch bothRunning = new CountDownLatch(2);
        executor.execute(new SyncedRunnable(bothRunning));
        executor.execute(new SyncedRunnable(bothRunning));
        waitAllDone(bothRunning);

        verify(callback).run();
    }

    @Test(timeout = TIMEOUT)
    public void multiple_executors_can_share_the_same_callback() throws InterruptedException {
        Executor executor1 = new MonitoredExecutor(realExecutor, workerCounter);
        Executor executor2 = new MonitoredExecutor(realExecutor, workerCounter);

        CountDownLatch bothRunning = new CountDownLatch(2);
        executor1.execute(new SyncedRunnable(bothRunning));
        executor2.execute(new SyncedRunnable(bothRunning));
        waitAllDone(bothRunning);

        verify(callback).run();
    }

    @Test(timeout = TIMEOUT)
    public void callback_is_called_even_when_commands_throw_exceptions() throws InterruptedException {
        Executor executor = new MonitoredExecutor(realExecutor, workerCounter);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                throw uncaughtExceptions.throwDummyException();
            }
        });
        waitAllDone();

        verify(callback).run();
    }


    private void waitAllDone(CountDownLatch bothRunning) throws InterruptedException {
        bothRunning.await(TIMEOUT, TimeUnit.MILLISECONDS);
        waitAllDone();
    }

    private void waitAllDone() throws InterruptedException {
        realExecutor.shutdown();
        realExecutor.awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);
    }

    private static class SyncedRunnable implements Runnable {
        private final CountDownLatch barrier;

        public SyncedRunnable(CountDownLatch barrier) {
            this.barrier = barrier;
        }

        @Override
        public void run() {
            barrier.countDown();
            try {
                barrier.await(TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
