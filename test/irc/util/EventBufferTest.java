package irc.util;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Supplier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import util.config.AppInjector;
import util.config.Environment;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.stream.IntStream;

/**
 * Created by Dominic Hauton on 17/03/2016.
 */
public class EventBufferTest {

    private EventBuffer eventBuffer;
    private Logger log = LogManager.getLogger();

    @Before
    public void setUp() {
        Injector injector = Guice.createInjector(new AppInjector(Environment.PROD));
        EventBufferFactory eventBufferFactory = injector.getInstance(EventBufferFactory.class);
        eventBuffer = eventBufferFactory.create(10, 1);
    }

    @Test
    public void TestEventRate() throws InterruptedException{
        IntStream.range(0, 10).forEach(value -> {
            Assert.assertTrue("Adding message " + value, eventBuffer.addMessage());
            log.info("Adding message " + value + ". Expect Accept.");
        });
        Thread.sleep(990);
        log.info("Adding message. Expect Reject.");
        Assert.assertFalse("Sending message before time up", eventBuffer.addMessage());
        Thread.sleep(20);
        IntStream.range(10, 20).forEach(value -> {
            Assert.assertTrue("Adding message " + value, eventBuffer.addMessage());
            log.info("Adding message " + value + ". Expect Accept.");
        });
        Thread.sleep(990);
        log.info("Adding message. Expect Reject.");
        Assert.assertFalse("Sending message before time up", eventBuffer.addMessage());
    }

    /**
     * Fails if method is not synchronized
     */
    @Test
    public void TestEventRateMultiThread() throws InterruptedException{
        Semaphore semaphore = new Semaphore(0);
        Supplier<Boolean> callEventBuffer = () -> {
            try{
                semaphore.acquire(1);
            } catch (InterruptedException e) {
                Assert.fail("Interrupted Exception during test.");
            }
            return eventBuffer.addMessage();
        };

        IntStream.range(0, 10).forEach(value -> {
            new CompletableFuture<>().thenRunAsync( () -> Assert.assertTrue("Adding message " + value, callEventBuffer.get()));
            log.info("Adding message " + value + ". Expect Accept.");
        });
        semaphore.release(10);

        Thread.sleep(995);
        log.info("Adding message. Expect Reject.");
        Assert.assertFalse("Sending message before time up", eventBuffer.addMessage());
        Thread.sleep(10);

        IntStream.range(10, 20).forEach(value -> {
            new CompletableFuture<>().thenRunAsync( () -> Assert.assertTrue("Adding message " + value, callEventBuffer.get()));
            log.info("Adding message " + value + ". Expect Accept.");
        });
        semaphore.release(10);

        Thread.sleep(995);
        log.info("Adding message. Expect Reject.");
        Assert.assertFalse("Sending message before time up", eventBuffer.addMessage());
    }
}
