package bot.channel.timeouts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Duration;

import java.util.HashMap;

/**
 * Created by Dominic Hauton on 15/04/2016.
 *
 * Manages all user timeouts for channel
 */
public class TimeoutManager {

  private static final Logger log = LogManager.getLogger();
  private HashMap<String, Duration> userTimeoutHistory;

  public TimeoutManager() {
    userTimeoutHistory = new HashMap<>();
  }

  public Duration getUserTimeout(String twitchUser) {
    Duration userTimeoutDuration = userTimeoutHistory.getOrDefault(twitchUser, Duration.ZERO);
    log.debug("Retrieving timeout for user {} from TimeoutManager. Current: {}", twitchUser::toString,
        userTimeoutDuration::toString);
    return userTimeoutDuration;
  }

  /**
   * Adds the timeout and calculates new total time
   *
   * @return new timeout time
   */
  public synchronized Duration addUserTimeout(String twitchUser, TimeoutReason timeoutReason) {
    Duration previousTimeout = getUserTimeout(twitchUser);
    Integer newTimeoutSeconds = Math.round(previousTimeout.toStandardSeconds().getSeconds() * timeoutReason.getMultiplier()) +
        timeoutReason.getTimeout().toStandardSeconds().getSeconds();
    Duration newTimeout = Duration.standardSeconds(newTimeoutSeconds);
    log.info("Adding user Timeout for {}. Previous was {} therefore new is {}", twitchUser::toString,
        previousTimeout::toString, newTimeout::toString);
    userTimeoutHistory.put(twitchUser, newTimeout);
    return newTimeout;
  }
}
