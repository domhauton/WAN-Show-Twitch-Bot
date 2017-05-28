package com.domhauton.wanbot.bot.channel.message;

import com.domhauton.wanbot.bot.channel.TwitchUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.facet.taxonomy.LRUHashMap;


/**
 * Created by Dominic Hauton on 12/03/2016.
 * <p>
 * A rolling log of messages with snapshot retrieval.
 */
public class MessageManager {
  private static final Logger log = LogManager.getLogger();
  private static final int maxUserQueueSize = 256;
  private final int userQueueSize;
  private TwitchMessageEvictingQueue channelEvictingQueue;
  private LRUHashMap<TwitchUser, TwitchMessageEvictingQueue> userEvictingQueueMap;

  public MessageManager() {
    this(10, 100);
  }

  MessageManager(int userQueueSize, int channelQueueSize) {
    this.userQueueSize = userQueueSize;

    channelEvictingQueue = new TwitchMessageEvictingQueue(channelQueueSize);
    userEvictingQueueMap = new LRUHashMap<>(maxUserQueueSize);

    log.info("Created MessageManager. User queue size: {}. Channel queue size: {}", userQueueSize, channelQueueSize);
  }


  public ImmutableTwitchMessageList getChannelSnapshot() {
    ImmutableTwitchMessageList messageBufferSnapshot = channelEvictingQueue.getMessageBufferSnapshot();
    log.debug("Retrieving channel snapshot. Current size: {}", messageBufferSnapshot::size);
    return messageBufferSnapshot;
  }

  public ImmutableTwitchMessageList getUserSnapshot(TwitchUser user) {
    ImmutableTwitchMessageList messageBufferSnapshot = userEvictingQueueMap.getOrDefault(user, new TwitchMessageEvictingQueue(userQueueSize))
        .getMessageBufferSnapshot();
    log.debug("Retrieving user {} snapshot. Current size: {}", user::toString, messageBufferSnapshot::size);
    return messageBufferSnapshot;
  }

  /**
   * Inserts a MESSAGE into the MESSAGE manager
   *
   * @return true if insertion was successful.
   */
  public synchronized boolean addMessage(TwitchMessage twitchMessage) {
    log.debug("Storing message in MessageManager. Message: {}", twitchMessage::toString);
    userEvictingQueueMap.computeIfAbsent(twitchMessage.getTwitchUser(), key -> new TwitchMessageEvictingQueue(userQueueSize));
    return channelEvictingQueue.addMessage(twitchMessage)
        && userEvictingQueueMap.get(twitchMessage.getTwitchUser()).addMessage(twitchMessage);
  }
}
