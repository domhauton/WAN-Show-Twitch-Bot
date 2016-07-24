package twitch.channel.message;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.facet.taxonomy.LRUHashMap;
import org.apache.lucene.search.LRUQueryCache;
import twitch.channel.TwitchUser;

import java.util.LinkedHashMap;


/**
 * Created by Dominic Hauton on 12/03/2016.
 * <p>
 * A rolling log of messages with snapshot retrieval.
 */
public class MessageManager {
    private static final Logger s_log = LogManager.getLogger();
    private static final int s_maxUserQueueSize = 256;

    private TwitchMessageEvictingQueue m_channelEvictingQueue;
    private LRUHashMap<TwitchUser, TwitchMessageEvictingQueue> m_userEvictingQueueMap;

    private final int userQueueSize;

    public MessageManager() {
        this(10, 100);
    }

    MessageManager(int userQueueSize, int channelQueueSize) {
        this.userQueueSize = userQueueSize;

        m_channelEvictingQueue = new TwitchMessageEvictingQueue(channelQueueSize);
        m_userEvictingQueueMap = new LRUHashMap<>(s_maxUserQueueSize);

        s_log.info("Created MessageManager. User queue size: {}. Channel queue size: {}", userQueueSize, channelQueueSize);
    }


    public ImmutableTwitchMessageList getChannelSnapshot() {
        return m_channelEvictingQueue.getMessageBufferSnapshot();
    }

    public ImmutableTwitchMessageList getUserSnapshot(TwitchUser user) {
        return m_userEvictingQueueMap.getOrDefault(user, new TwitchMessageEvictingQueue(userQueueSize))
                .getMessageBufferSnapshot();
    }

    /**
     * Inserts a MESSAGE into the MESSAGE manager
     *
     * @return true if insertion was successful.
     */
    public synchronized boolean addMessage(TwitchMessage twitchMessage) {
        s_log.debug("Storing message in MessageManager. Message: {}", twitchMessage::toString);
        m_userEvictingQueueMap.computeIfAbsent(twitchMessage.getTwitchUser(), key -> new TwitchMessageEvictingQueue(userQueueSize));
        return m_channelEvictingQueue.addMessage(twitchMessage)
               && m_userEvictingQueueMap.get(twitchMessage.getTwitchUser()).addMessage(twitchMessage);
    }
}
