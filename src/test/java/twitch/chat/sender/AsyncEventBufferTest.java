package twitch.chat.sender;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Supplier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import twitch.chat.sender.AsyncEventBuffer;

import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Dominic Hauton on 17/03/2016.
 *
 * Tests the AsyncEventBuffer. Tests may fail unexpectedly due to time limitations.
 */
public class AsyncEventBufferTest {

    private AsyncEventBuffer asyncEventBuffer;
    private Logger log = LogManager.getLogger();

    @Before
    public void setUp() {
        asyncEventBuffer = new AsyncEventBuffer(10, 1);
    }

    @Test
    public void testEventRate() throws InterruptedException{
        IntStream.range(0, 10).forEachOrdered(value -> {
            Assert.assertTrue("Adding message " + value, asyncEventBuffer.addMessage());
            log.info("Adding message " + value + ". Expect Accept.");
        });
        Thread.sleep(950);
        log.info("Adding message. Expect Reject.");
        Assert.assertFalse("Sending message before time up", asyncEventBuffer.addMessage());
        Thread.sleep(100);
        IntStream.range(10, 20).forEachOrdered(value -> {
            Assert.assertTrue("Adding message " + value, asyncEventBuffer.addMessage());
            log.info("Adding message " + value + ". Expect Accept.");
        });
        Thread.sleep(950);
        log.info("Adding message. Expect Reject.");
        Assert.assertFalse("Sending message before time up", asyncEventBuffer.addMessage());
    }

    /**
     * Fails if method is not synchronized
     */
    @Test
    public void testEventRateMultiThread() throws InterruptedException{
        Semaphore semaphore = new Semaphore(0);

        Supplier<Boolean> callEventBuffer = () -> {
            try{
                semaphore.acquire(1);
                return asyncEventBuffer.addMessage();
            } catch (InterruptedException e) {
                Assert.fail("Interrupted Exception during test.");
                return false;
            }
        };

        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        IntStream.range(0, 20)
                .mapToObj(value -> CompletableFuture
                    .supplyAsync(callEventBuffer::get, threadPool)
                    .thenAccept(result -> Assert.assertTrue("Adding message " + value, result)))
                .collect(Collectors.toList()); // Required to force evaluation
        semaphore.release(10);

        Thread.sleep(950); // Release 10 messages
        Assert.assertFalse("Sending message before time up", asyncEventBuffer.addMessage());
        Thread.sleep(100);
        semaphore.release(10); // Release 10 messages
        Thread.sleep(950);
        Assert.assertFalse("Sending message before time up", asyncEventBuffer.addMessage());
    }
}
