package bot.channel.timeouts;

import org.joda.time.Duration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.stream.Stream;

/**
 * Created by Dominic Hauton on 22/08/2016.
 * <p>
 * E
 */
public class TimeoutManagerTest {

    private TimeoutManager m_timeoutManager;

    private static String s_username = "foobarname1";

    @Before
    public void setUp() throws Exception {
        m_timeoutManager = new TimeoutManager();
    }

    @Test
    public void simpleAddTimeTest() throws Exception {
        Duration actualReturnedTimeout = m_timeoutManager.addUserTimeout(s_username, TimeoutReason.MESSAGE_REPETITION);
        Duration actualTimeout = m_timeoutManager.getUserTimeout(s_username);
        Assert.assertEquals("Timeout should be same as return", actualReturnedTimeout, actualTimeout);
        Assert.assertEquals("Timeout should be same as original", TimeoutReason.MESSAGE_REPETITION.getTimeout(), actualTimeout);
    }

    @Test
    public void timeoutMultiplierTest() throws Exception {
        simpleAddTimeTest();
        Duration actualReturnedTimeout = m_timeoutManager.addUserTimeout(s_username, TimeoutReason.MESSAGE_REPETITION);
        Duration actualTimeout = m_timeoutManager.getUserTimeout(s_username);
        Integer expectedTimeoutSeconds = Math.round(
                TimeoutReason.MESSAGE_REPETITION.getTimeout().getStandardSeconds() * (1
                                                                                      + TimeoutReason.MESSAGE_REPETITION
                                                                                              .getMultiplier()));
        Duration expectedTimeout = Duration.standardSeconds(expectedTimeoutSeconds);
        Assert.assertEquals("Timeout should be same as return", actualReturnedTimeout, actualTimeout);
        Assert.assertEquals("Timeout should be same as original with multiplier applied", expectedTimeout, actualTimeout);
    }

    @Test
    public void timeoutMessageTest() throws Exception {
        Stream.of(TimeoutReason.values())
                .map(TimeoutReason::getMessage)
                .forEach(s -> Assert.assertFalse("TimeoutMessage should never be empty", s.isEmpty()));

    }
}