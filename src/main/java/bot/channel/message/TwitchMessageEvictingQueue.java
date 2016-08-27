package bot.channel.message;

import com.google.common.collect.EvictingQueue;

import java.util.Iterator;
import java.util.Optional;

/**
 * Created by Dominic Hauton on 13/03/2016.
 *
 * Decorator for an Evicting queue
 */
class TwitchMessageEvictingQueue {
  private EvictingQueue<TwitchMessage> messageBuffer;

  TwitchMessageEvictingQueue(int bufferLength) {
    messageBuffer = EvictingQueue.create(bufferLength);
  }

  ImmutableTwitchMessageList getMessageBufferSnapshot() {
    return new ImmutableTwitchMessageList(messageBuffer);
  }

  /**
   * Gets an optional containing the most recent message.
   */
  public Optional<TwitchMessage> getMostRecentMessage() {
    Iterator<TwitchMessage> messageIterator = messageBuffer.iterator();
    TwitchMessage finalMessage = null;
    while (messageIterator.hasNext()) {
      finalMessage = messageIterator.next();
    }
    return Optional.ofNullable(finalMessage);
  }

  /**
   * Offers the message.
   *
   * @return False if insertion failed. EvictingQueue offer should never fail!
   */
  public synchronized boolean addMessage(TwitchMessage message) {
    return messageBuffer.offer(message);
  }
}
