package com.domhauton.wanbot.bot.channel.message;

import com.domhauton.wanbot.bot.channel.TwitchUser;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Dominic Hauton on 20/03/2016.
 * <p>
 * Testing the effectiveness of the async MESSAGE buffer.
 */
class TwitchMessageEvictingQueueTest {

  private TwitchMessageEvictingQueue twitchMessageEvictingQueue;
  private Semaphore semaphore;

  @BeforeEach
  void setUp() throws Exception {
    semaphore = new Semaphore(0);
    twitchMessageEvictingQueue = new TwitchMessageEvictingQueue(5);
  }

  @Test
  void testMessageAddition() throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(5);
    addMessagesAndWait(0, 3, pool);
    Assertions.assertEquals(3, twitchMessageEvictingQueue.getMessageBufferSnapshot().size(), "Assert no collisions");
    addMessagesAndWait(3, 5, pool);
    Assertions.assertEquals(5, twitchMessageEvictingQueue.getMessageBufferSnapshot().size(),
        "Assert can go up to max size");
    addMessagesAndWait(5, 20, pool);
    Assertions.assertEquals(5, twitchMessageEvictingQueue.getMessageBufferSnapshot().size(),
        "Assert no overflow");
  }

  @Test
  void testMessageRemoval() throws Exception {
    Collection<TwitchMessage> firstMessageBatch = generateTwitchMessages(0, 3);

    firstMessageBatch.stream().forEachOrdered(message -> twitchMessageEvictingQueue.addMessage(message));

    Collection<TwitchMessage> actualFirstMessageBatch = twitchMessageEvictingQueue
        .getMessageBufferSnapshot()
        .stream()
        .collect(Collectors.toList());

    Assertions.assertEquals(firstMessageBatch, actualFirstMessageBatch,
        "Ensure 3 messages added correctly.");

    Collection<TwitchMessage> secondMessageBatch = generateTwitchMessages(4, 9);

    secondMessageBatch.stream().forEachOrdered(message -> twitchMessageEvictingQueue.addMessage(message));

    Collection<TwitchMessage> actualSecondMessageBatch = twitchMessageEvictingQueue
        .getMessageBufferSnapshot()
        .stream()
        .collect(Collectors.toList());

    Assertions.assertEquals(secondMessageBatch, actualSecondMessageBatch,
        "Assert messages replace on another correctly.");
  }

  @Test
  void getMostRecentMessageSimpleTest() throws Exception {
    Collection<TwitchMessage> twitchMessages = generateTwitchMessages(0, 10);
    TwitchMessage expectedTwitchMessage = new TwitchMessage("foo", new TwitchUser("foo"), DateTime.now(), "foo");
    twitchMessages.forEach(twitchMessage -> twitchMessageEvictingQueue.addMessage(twitchMessage));
    twitchMessageEvictingQueue.addMessage(expectedTwitchMessage);
    TwitchMessage actualTwitchMessage = twitchMessageEvictingQueue.getMostRecentMessage().orElse(null);
    Assertions.assertEquals(expectedTwitchMessage, actualTwitchMessage);
  }

  @Test
  void getMostRecentMessageEmptyTest() throws Exception {
    Optional<TwitchMessage> actualTwitchMessage = twitchMessageEvictingQueue.getMostRecentMessage();
    Assertions.assertFalse(actualTwitchMessage.isPresent());
  }

  /**
   * Sends a TwitchMessage for 1 semaphore permit
   *
   * @param twitchMessage TwitchMessage to send
   * @return whether or not adding was successful
   */
  private boolean sendMessageOnSemaphore(TwitchMessage twitchMessage, CountDownLatch latch) {
    try {
      semaphore.acquire();
      boolean success = twitchMessageEvictingQueue.addMessage(twitchMessage);
      latch.countDown();
      return success;
    } catch (InterruptedException e) {
      Assertions.fail("Interrupted before able to send TwitchMessage.");
      return false;
    }
  }

  /**
   * Creates all of the MESSAGE actions, then releases them to execute as asyncronously as possible.
   *
   * @param twitchMessages Messages to send.
   * @param pool           ThreadPool that will send messages.
   * @param latch          Latch that MESSAGE addition should trigger.
   */
  private void addMessagesToPoolAndRelease(Collection<TwitchMessage> twitchMessages, ExecutorService pool, CountDownLatch latch) {
    twitchMessages.stream()
        .map(message -> CompletableFuture.supplyAsync(() -> sendMessageOnSemaphore(message, latch), pool))
        .collect(Collectors.toList()); // Force evaluation.
    semaphore.release(twitchMessages.size());
  }

  /**
   * Generate a series of TwitchMessages
   *
   * @param startMessage id of first MESSAGE
   * @param endMessage   Exclusive id of last MESSAGE.
   * @return Collection of generated messages
   */
  private Collection<TwitchMessage> generateTwitchMessages(int startMessage, int endMessage) {
    return IntStream.range(startMessage, endMessage)
        .mapToObj(String::valueOf)
        .map(message -> "Message Number " + message)
        .map(message -> new TwitchMessage(message, new TwitchUser("foobar"), DateTime.now(), "#foochannel"))
        .collect(Collectors.toList());
  }

  private void addMessagesAndWait(int startMessage, int endMessage, ExecutorService pool) throws Exception {
    Collection<TwitchMessage> twitchMessagesTest1 = generateTwitchMessages(startMessage, endMessage);
    CountDownLatch latchTest1 = new CountDownLatch(twitchMessagesTest1.size());
    addMessagesToPoolAndRelease(twitchMessagesTest1, pool, latchTest1);
    latchTest1.await(1L, TimeUnit.SECONDS);
  }
}