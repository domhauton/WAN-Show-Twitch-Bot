package channel.message;

import com.google.common.collect.EvictingQueue;

import java.util.concurrent.CompletableFuture;

/**
 * Created by Dominic Hauton on 13/03/2016.
 *
 * An a
 */
public class twitchMessageBuffer {
    private EvictingQueue<TwitchMessage> messageBuffer;

    public twitchMessageBuffer(int bufferLength) {
        messageBuffer = EvictingQueue.create(bufferLength);
    }

    public ImmutableTwitchMessageList getMessageBufferSnapshot() {
        return new ImmutableTwitchMessageList( messageBuffer );
    }

    public synchronized boolean addMessage(TwitchMessage message) {
        return messageBuffer.offer(message);
    }
}
