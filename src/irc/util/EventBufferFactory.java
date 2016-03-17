package irc.util;

import com.google.inject.assistedinject.Assisted;

/**
 * Created by Dominic Hauton on 17/03/2016.
 *
 * Factory for injecting EventBuffers
 */
public interface EventBufferFactory {
    EventBuffer create(
            @Assisted("maxEventCount") Integer maxEventCount,
            @Assisted("eventLifeTimeSeconds") Integer eventLifeTimeSeconds);
}
