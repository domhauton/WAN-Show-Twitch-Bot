package channel.message;

import channel.data.TwitchMessage;
import com.google.common.collect.EvictingQueue;

/**
 * Created by Dominic Hauton on 13/03/2016.
 *
 * Decorator for an Evicting queue
 */
public class TwitchMessageEvictingQueue {
    private EvictingQueue<TwitchMessage> messageBuffer;

    public TwitchMessageEvictingQueue(int bufferLength) {
        messageBuffer = EvictingQueue.create(bufferLength);
    }

    public ImmutableTwitchMessageList getMessageBufferSnapshot() {
        return new ImmutableTwitchMessageList( messageBuffer );
    }

    public synchronized boolean addMessage(TwitchMessage message) {
        return messageBuffer.offer(message);
    }
}
