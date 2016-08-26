package twitch.channel.message;

import com.google.common.collect.EvictingQueue;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Created by Dominic Hauton on 13/03/2016.
 *
 * Decorator for an Evicting queue
 */
class TwitchMessageEvictingQueue {
    private EvictingQueue<TwitchMessage> m_messageBuffer;

    TwitchMessageEvictingQueue(int bufferLength) {
        m_messageBuffer = EvictingQueue.create(bufferLength);
    }

    ImmutableTwitchMessageList getMessageBufferSnapshot() {
        return new ImmutableTwitchMessageList(m_messageBuffer);
    }

    /**
     * Gets an optional containing the most recent message.
     */
    public Optional<TwitchMessage> getMostRecentMessage() {
        Iterator<TwitchMessage> messageIterator = m_messageBuffer.iterator();
        TwitchMessage finalMessage = null;
        while(messageIterator.hasNext()){
            finalMessage = messageIterator.next();
        }
        return Optional.ofNullable(finalMessage);
    }

    /**
     * Offers the message.
     * @return False if insertion failed. EvictingQueue offer should never fail!
     */
    public synchronized boolean addMessage(TwitchMessage message) {
        return m_messageBuffer.offer(message);
    }
}
