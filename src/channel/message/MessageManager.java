package channel.message;

import channel.users.TwitchUser;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Dominic Hauton on 12/03/2016.
 */
public class MessageManager {
    private TwitchMessageEvictingQueue channelEvictingQueue;
    private ConcurrentMap<String, TwitchMessageEvictingQueue> userEvictingQueueMap;

    private final static int userQueueSize = 10;
    private final static int channelQueueSize = 10;

    public MessageManager() {
        channelEvictingQueue = new TwitchMessageEvictingQueue(channelQueueSize);
        userEvictingQueueMap = new ConcurrentHashMap<>();
    }

    public ImmutableTwitchMessageList getChannelSnapshot() {
        return channelEvictingQueue.getMessageBufferSnapshot();
    }

    public ImmutableTwitchMessageList getUserSnapshot(String twitchUserName) {
        return userEvictingQueueMap.get(twitchUserName).getMessageBufferSnapshot();
    }

    public ImmutableTwitchMessageList getUserSnapshot(TwitchUser twitchUser) {
        return getUserSnapshot(generateTwitchUserKey(twitchUser));
    }

    /**
     * Inserts a message into the message manager
     * @param twitchMessage
     * @return
     */
    public synchronized boolean addMessage(TwitchMessage twitchMessage) {
       userEvictingQueueMap
                .putIfAbsent(twitchMessage.getSender().getUsername(), new TwitchMessageEvictingQueue(userQueueSize));
        return channelEvictingQueue.addMessage(twitchMessage)
                && userEvictingQueueMap.get(generateTwitchUserKey(twitchMessage.getSender())).addMessage(twitchMessage);
    }

    private String generateTwitchUserKey(TwitchUser twitchUser) {
        return twitchUser.getUsername();
    }
}
