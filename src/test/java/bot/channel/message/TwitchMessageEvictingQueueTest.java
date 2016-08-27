package bot.channel.message;

import bot.channel.TwitchUser;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Dominic Hauton on 20/03/2016.
 *
 * Testing the effectiveness of the async MESSAGE buffer.
 */
public class TwitchMessageEvictingQueueTest {

    private TwitchMessageEvictingQueue twitchMessageEvictingQueue;
    private Semaphore semaphore;

    /**
     * Sends a TwitchMessage for 1 semaphore permit
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
            Assert.fail("Interrupted before able to send TwitchMessage.");
            return false;
        }
    }

    /**
     * Creates all of the MESSAGE actions, then releases them to execute as asyncronously as possible.
     * @param twitchMessages    Messages to send.
     * @param pool              ThreadPool that will send messages.
     * @param latch             Latch that MESSAGE addition should trigger.
     */
    private void addMessagesToPoolAndRelease(Collection<TwitchMessage> twitchMessages, ExecutorService pool, CountDownLatch latch) {
        twitchMessages.stream()
                .map(message -> CompletableFuture.supplyAsync(() -> sendMessageOnSemaphore(message, latch), pool))
                .collect(Collectors.toList()); // Force evaluation.
        semaphore.release(twitchMessages.size());
    }

    /**
     * Generate a series of TwitchMessages
     * @param startMessage  id of first MESSAGE
     * @param endMessage    Exclusive id of last MESSAGE.
     * @return              Collection of generated messages
     */
    private Collection<TwitchMessage> generateTwitchMessages(int startMessage, int endMessage) {
        return IntStream.range(startMessage, endMessage)
                .mapToObj(String::valueOf)
                .map(message -> "Message Number " + message )
                .map(message ->  new TwitchMessage(message, new TwitchUser("foobar"), DateTime.now(), "#foochannel"))
                .collect(Collectors.toList());
    }

    private void addMessagesAndWait(int startMessage, int endMessage, ExecutorService pool) throws Exception {
        Collection<TwitchMessage> twitchMessagesTest1 = generateTwitchMessages(startMessage, endMessage);
        CountDownLatch latchTest1 = new CountDownLatch(twitchMessagesTest1.size());
        addMessagesToPoolAndRelease(twitchMessagesTest1, pool, latchTest1);
        latchTest1.await(1L, TimeUnit.SECONDS);
    }

    @Before
    public void setUp() throws Exception {
        semaphore = new Semaphore(0);
        twitchMessageEvictingQueue = new TwitchMessageEvictingQueue(5);
    }

    @Test
    public void testMessageAddition() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(5);
        addMessagesAndWait(0, 3, pool);
        Assert.assertEquals("Assert no collisions", 3, twitchMessageEvictingQueue.getMessageBufferSnapshot().size());
        addMessagesAndWait(3, 5, pool);
        Assert.assertEquals("Assert can go up to max size",
                5, twitchMessageEvictingQueue.getMessageBufferSnapshot().size());
        addMessagesAndWait(5, 20, pool);
        Assert.assertEquals("Assert no overflow", 5, twitchMessageEvictingQueue.getMessageBufferSnapshot().size());
    }

    @Test
    public void testMessageRemoval() throws Exception {
        Collection<TwitchMessage> firstMessageBatch = generateTwitchMessages(0, 3);

        firstMessageBatch.stream().forEachOrdered(message -> twitchMessageEvictingQueue.addMessage(message));

        Collection<TwitchMessage> actualFirstMessageBatch = twitchMessageEvictingQueue
                .getMessageBufferSnapshot()
                .stream()
                .collect(Collectors.toList());

        Assert.assertEquals("Ensure 3 messages added correctly.", firstMessageBatch, actualFirstMessageBatch);

        Collection<TwitchMessage> secondMessageBatch = generateTwitchMessages(4, 9);

        secondMessageBatch.stream().forEachOrdered(message -> twitchMessageEvictingQueue.addMessage(message));

        Collection<TwitchMessage> actualSecondMessageBatch = twitchMessageEvictingQueue
                .getMessageBufferSnapshot()
                .stream()
                .collect(Collectors.toList());

        Assert.assertEquals("Assert messages replace on another correctly.",
                secondMessageBatch, actualSecondMessageBatch);
    }

    @Test
    public void getMostRecentMessageSimpleTest() throws Exception {
        Collection<TwitchMessage> twitchMessages = generateTwitchMessages(0, 10);
        TwitchMessage expectedTwitchMessage = new TwitchMessage("foo", new TwitchUser("foo"), DateTime.now(), "foo");
        twitchMessages.forEach(twitchMessage -> twitchMessageEvictingQueue.addMessage(twitchMessage));
        twitchMessageEvictingQueue.addMessage(expectedTwitchMessage);
        TwitchMessage actualTwitchMessage = twitchMessageEvictingQueue.getMostRecentMessage().orElse(null);
        Assert.assertEquals(expectedTwitchMessage, actualTwitchMessage);
    }

    @Test
    public void getMostRecentMessageEmptyTest() throws Exception {
        Optional<TwitchMessage> actualTwitchMessage = twitchMessageEvictingQueue.getMostRecentMessage();
        Assert.assertFalse(actualTwitchMessage.isPresent());
    }
}