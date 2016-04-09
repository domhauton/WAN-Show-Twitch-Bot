package channel.message;

import channel.data.TwitchMessage;
import channel.data.TwitchUser;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Dominic Hauton on 12/03/2016.
 *
 * A rolling log of messages with snapshot retrieval.
 */
public class MessageManager {
    private TwitchMessageEvictingQueue channelEvictingQueue;
    private ConcurrentMap<TwitchUser, TwitchMessageEvictingQueue> userEvictingQueueMap;

    private final static int userQueueSize = 10;
    private final static int channelQueueSize = 10;

    public MessageManager() {
        channelEvictingQueue = new TwitchMessageEvictingQueue(channelQueueSize);
        userEvictingQueueMap = new ConcurrentHashMap<>();
    }


    public ImmutableTwitchMessageList getChannelSnapshot() {
        return channelEvictingQueue.getMessageBufferSnapshot();
    }

    public ImmutableTwitchMessageList getUserSnapshot(TwitchUser user) {
        return userEvictingQueueMap.get(user).getMessageBufferSnapshot();
    }

    /**
     * Inserts a message into the message manager
     * @return true if insertion was successful.
     */
    public synchronized boolean addMessage(TwitchMessage twitchMessage) {
       userEvictingQueueMap
                .putIfAbsent(twitchMessage.getSender(), new TwitchMessageEvictingQueue(userQueueSize));
        return channelEvictingQueue.addMessage(twitchMessage)
                && userEvictingQueueMap.get(twitchMessage.getSender()).addMessage(twitchMessage);
    }
}
