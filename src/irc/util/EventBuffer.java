package irc.util;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import java.util.Arrays;

/**
 * Created by Dominic Hauton on 16/03/2016.
 *
 * Fast event buffer for counting events.
 */
public class EventBuffer {

    private long[] messageQueue;
    private int maxEventCount;
    private int eventLifeTimeMillis;

    private int iteratorIndex = 0;

    @AssistedInject
    public EventBuffer(
            @Assisted("maxEventCount") Integer maxEventCount,
            @Assisted("eventLifeTimeSeconds") Integer eventLifeTimeSeconds) {
        messageQueue = new long[maxEventCount];
        this.maxEventCount = maxEventCount;
        eventLifeTimeMillis = eventLifeTimeSeconds * 1000;
        Arrays.fill(messageQueue, System.currentTimeMillis() - eventLifeTimeMillis);
    }

    /**
     * Add a message to the event Buffer
     * @return true if event added. False if addition failed.
     */
    public synchronized boolean addMessage() {
        if (messageQueue[iteratorIndex] < System.currentTimeMillis()) {
            messageQueue[iteratorIndex++] = System.currentTimeMillis() + eventLifeTimeMillis;
            if(iteratorIndex == maxEventCount) iteratorIndex = 0;
            return true;
        } else {
            return false;
        }
    }
}
