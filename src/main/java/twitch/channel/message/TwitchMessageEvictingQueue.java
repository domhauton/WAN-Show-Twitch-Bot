package twitch.channel.message;

import com.google.common.collect.EvictingQueue;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Created by Dominic Hauton on 13/03/2016.
 *
 * Decorator for an Evicting queue
 */
public class TwitchMessageEvictingQueue {
    private EvictingQueue<TwitchMessage> m_messageBuffer;

    public TwitchMessageEvictingQueue(int bufferLength) {
        m_messageBuffer = EvictingQueue.create(bufferLength);
    }

    public ImmutableTwitchMessageList getMessageBufferSnapshot() {
        return new ImmutableTwitchMessageList(m_messageBuffer);
    }

    /**
     * Gets an optional containing the most recent message.
     * @return
     */
    public Optional<TwitchMessage> getMostRecentMessage() {
        try {
            return Optional.of(m_messageBuffer.iterator().next());
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }

    public synchronized boolean addMessage(TwitchMessage message) {
        return m_messageBuffer.offer(message);
    }
}
