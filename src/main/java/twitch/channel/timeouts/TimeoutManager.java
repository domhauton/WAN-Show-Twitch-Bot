package twitch.channel.timeouts;

import org.joda.time.Period;
import twitch.channel.data.TwitchUser;

import java.util.HashMap;

/**
 * Created by Dominic Hauton on 15/04/2016.
 *
 * Manages all user timeouts for channel
 */
public class TimeoutManager {

    private HashMap<TwitchUser, Period> userTimeoutHistory;

    public TimeoutManager() {
        userTimeoutHistory = new HashMap<>();
    }

    public Period getUserTimeout(TwitchUser twitchUser) {
        return userTimeoutHistory.getOrDefault(twitchUser, Period.ZERO);
    }

    public void addUserTimeout(TwitchUser twitchUser, Period timeoutPeriod){
        userTimeoutHistory.put(twitchUser, timeoutPeriod);
    }
}
