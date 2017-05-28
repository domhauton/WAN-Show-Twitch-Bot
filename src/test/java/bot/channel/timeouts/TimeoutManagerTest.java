package bot.channel.timeouts;

import org.joda.time.Duration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

/**
 * Created by Dominic Hauton on 22/08/2016.
 * <p>
 * E
 */
class TimeoutManagerTest {

  private static String username = "foobarname1";
  private TimeoutManager timeoutManager;

  @BeforeEach
  void setUp() throws Exception {
    timeoutManager = new TimeoutManager();
  }

  @Test
  void simpleAddTimeTest() throws Exception {
    Duration actualReturnedTimeout = timeoutManager.addUserTimeout(username, TimeoutReason.MESSAGE_REPETITION);
    Duration actualTimeout = timeoutManager.getUserTimeout(username);
    Assertions.assertEquals(actualReturnedTimeout, actualTimeout, "Timeout should be same as return");
    Assertions.assertEquals(TimeoutReason.MESSAGE_REPETITION.getTimeout(), actualTimeout, "Timeout should be same as original");
  }

  @Test
  void timeoutMultiplierTest() throws Exception {
    simpleAddTimeTest();
    Duration actualReturnedTimeout = timeoutManager.addUserTimeout(username, TimeoutReason.MESSAGE_REPETITION);
    Duration actualTimeout = timeoutManager.getUserTimeout(username);
    Integer expectedTimeoutSeconds = Math.round(
        TimeoutReason.MESSAGE_REPETITION.getTimeout().getStandardSeconds() * (1
            + TimeoutReason.MESSAGE_REPETITION
            .getMultiplier()));
    Duration expectedTimeout = Duration.standardSeconds(expectedTimeoutSeconds);
    Assertions.assertEquals(actualReturnedTimeout, actualTimeout, "Timeout should be same as return");
    Assertions.assertEquals(expectedTimeout, actualTimeout, "Timeout should be same as original with multiplier applied");
  }

  @Test
  void timeoutMessageTest() throws Exception {
    Stream.of(TimeoutReason.values())
        .map(TimeoutReason::getMessage)
        .forEach(s -> Assertions.assertFalse(s.isEmpty(), "TimeoutMessage should never be empty"));

  }
}