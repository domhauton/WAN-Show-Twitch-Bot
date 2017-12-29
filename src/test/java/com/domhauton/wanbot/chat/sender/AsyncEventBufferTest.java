package com.domhauton.wanbot.chat.sender;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Supplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.IntStream;

/**
 * Created by Dominic Hauton on 17/03/2016.
 * <p>
 * Tests the AsyncEventBuffer. Tests may fail unexpectedly due to time limitations.
 */
class AsyncEventBufferTest {

  private AsyncEventBuffer asyncEventBuffer;
  private Logger log = LogManager.getLogger();

  @BeforeEach
  void setUp() {
    asyncEventBuffer = new AsyncEventBuffer(10, 1);
  }

  @Test
  void testEventRate() throws InterruptedException {
    IntStream.range(0, 10).forEachOrdered(value -> {
      Assertions.assertTrue(asyncEventBuffer.addMessage(), "Adding MESSAGE " + value);
      log.info("Adding MESSAGE " + value + ". Expect Accept.");
    });

    Thread.sleep(700);
    log.info("Adding MESSAGE. Expect Reject.");
    Assertions.assertFalse(asyncEventBuffer.addMessage(), "Sending MESSAGE 1 before time up");
    Thread.sleep(400);

    IntStream.range(10, 20).forEachOrdered(value -> {
      Assertions.assertTrue(asyncEventBuffer.addMessage(), "Adding MESSAGE " + value);
      log.info("Adding MESSAGE " + value + ". Expect Accept.");
    });
    Thread.sleep(700);
    log.info("Adding MESSAGE. Expect Reject.");
    Assertions.assertFalse(asyncEventBuffer.addMessage(), "Sending MESSAGE 2 before time up");
  }

  /**
   * Fails if method is not synchronized
   */
  @Test
  void testEventRateMultiThread() throws InterruptedException {
    Semaphore semaphore = new Semaphore(0);

    Supplier<Boolean> callEventBuffer = () -> {
      try {
        semaphore.acquire(1);
        return asyncEventBuffer.addMessage();
      } catch (InterruptedException e) {
        Assertions.fail("Interrupted Exception during test.");
        return false;
      }
    };

    ExecutorService threadPool = Executors.newFixedThreadPool(10);
    IntStream.range(0, 20)
        .forEach(value -> CompletableFuture
            .supplyAsync(callEventBuffer::get, threadPool)
            .thenAccept(result -> Assertions.assertTrue(result, "Adding MESSAGE " + value))
        );
    semaphore.release(10);

    Thread.sleep(700); // Release 10 messages
    Assertions.assertFalse(asyncEventBuffer.addMessage(), "Sending MESSAGE 1 before time up");
    Thread.sleep(400);
    semaphore.release(10); // Release 10 messages
    Thread.sleep(700);
    Assertions.assertFalse(asyncEventBuffer.addMessage(), "Sending MESSAGE 2 before time up");
  }
}
