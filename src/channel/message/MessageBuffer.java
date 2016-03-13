package channel.message;

import com.google.common.collect.EvictingQueue;

import java.util.concurrent.CompletableFuture;

/**
 * Created by Dominic Hauton on 13/03/2016.
 */
public class MessageBuffer {
    private EvictingQueue<TwitchMessage> messageBuffer;

    public MessageBuffer(int bufferLength) {
        messageBuffer = EvictingQueue.create(bufferLength);
    }

    public ImmutableMessageList getMessageBufferSnapshot() {
        return new ImmutableMessageList( messageBuffer );
    }

    public CompletableFuture addMessageToBuffer(TwitchMessage message){
        return CompletableFuture.runAsync(() -> addMessage(message));
    }

    private synchronized boolean addMessage(TwitchMessage message) {
        return messageBuffer.add(message);
    }
}
