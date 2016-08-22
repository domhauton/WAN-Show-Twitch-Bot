package twitch.channel.timeouts;

import org.joda.time.Duration;

import java.util.HashMap;

/**
 * Created by Dominic Hauton on 15/04/2016.
 *
 * Manages all user timeouts for channel
 */
public class TimeoutManager {

    private HashMap<String, Duration> userTimeoutHistory;

    public TimeoutManager() {
        userTimeoutHistory = new HashMap<>();
    }

    public Duration getUserTimeout(String twitchUser) {
        return userTimeoutHistory.getOrDefault(twitchUser, Duration.ZERO);
    }

    /**
     * Adds the timeout and calculates new total time
     * @return new timeout time
     */
    public synchronized Duration addUserTimeout(String twitchUser, TimeoutReason timeoutReason){
        Duration previousTimeout = getUserTimeout(twitchUser);
        Integer newTimeoutSeconds = Math.round(previousTimeout.toStandardSeconds().getSeconds()*timeoutReason.getMultiplier()) +
                                    timeoutReason.getTimeout().toStandardSeconds().getSeconds();
        Duration newTimeout = Duration.standardSeconds(newTimeoutSeconds);
        userTimeoutHistory.put(twitchUser, newTimeout);
        return newTimeout;
    }
}
