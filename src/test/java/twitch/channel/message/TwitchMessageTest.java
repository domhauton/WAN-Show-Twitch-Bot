package twitch.channel.message;

import twitch.channel.TwitchUser;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Created by Dominic Hauton on 25/03/2016.
 *
 * Tests for the TwitchMessage data class
 */
public class TwitchMessageTest {
    private TwitchMessage twitchMessagePayload1User1_1;
    private TwitchMessage twitchMessagePayload1User1_2;
    private TwitchMessage twitchMessagePayload2User1;

    private final String payload1 = "P1yloAd foobar 1!";
    private final String payload2 = "P1yloAd foobar 2!";
    private final String payload1Simple = "p1yloadfoobar1!";
    private final String payload2Simple = "p1yloadfoobar2!";
    private final String twitchUsername = "user1";
    private final String sourceChannel = "#foochannel";
    private final DateTime baseDateTime = DateTime.now();

    @Before
    public void setUp() throws Exception {
        TwitchUser twitchUser = new TwitchUser(twitchUsername);
        twitchMessagePayload1User1_1 = new TwitchMessage(payload1, twitchUser, baseDateTime, sourceChannel);
        twitchMessagePayload1User1_2 = new TwitchMessage(payload1, twitchUser, baseDateTime, sourceChannel);
        twitchMessagePayload2User1 = new TwitchMessage(payload2, twitchUser, baseDateTime, sourceChannel);
    }

    @Test
    public void getMessagePayload() throws Exception {
        Assert.assertEquals(payload1, twitchMessagePayload1User1_1.getMessage());
        Assert.assertEquals(payload1, twitchMessagePayload1User1_2.getMessage());
        Assert.assertEquals(payload2, twitchMessagePayload2User1.getMessage());
    }

    @Test
    public void getSimpleMessagePayload() throws Exception {
        Assert.assertEquals(payload1Simple, twitchMessagePayload1User1_1.getSimpleMessagePayload());
        Assert.assertEquals(payload1Simple, twitchMessagePayload1User1_2.getSimpleMessagePayload());
        Assert.assertEquals(payload2Simple, twitchMessagePayload2User1.getSimpleMessagePayload());
    }

    @Test
    public void getSender() throws Exception {
        Assert.assertEquals(twitchUsername, twitchMessagePayload1User1_1.getUsername());
    }

    @Test
    public void getMessageDateTime() throws Exception {
        Assert.assertEquals(baseDateTime, twitchMessagePayload1User1_1.getMessageDateTime());
    }

    @Test
    public void getLegalCharRatio() throws Exception {
        Set<Character> legalChars = Stream.of( 'a', 'b' ).collect(Collectors.toSet());
        Assert.assertEquals(
                3d/twitchMessagePayload1User1_1.getSimpleMessagePayload().length(),
                twitchMessagePayload1User1_1.getLegalCharRatio(legalChars),
                0.05d);
    }

    @Test
    public void equalsSimplePayload() throws Exception {
        Assert.assertTrue(twitchMessagePayload1User1_1.equalsSimplePayload(payload1Simple));
        Assert.assertTrue(twitchMessagePayload1User1_1.equalsSimplePayload(payload1));
        Assert.assertFalse(twitchMessagePayload1User1_1.equalsSimplePayload(payload2));
        Assert.assertFalse(twitchMessagePayload1User1_1.equalsSimplePayload(payload2Simple));
    }
}