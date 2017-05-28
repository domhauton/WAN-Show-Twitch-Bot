package com.domhauton.wanbot.bot.channel.message;

import com.domhauton.wanbot.bot.channel.TwitchUser;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Created by Dominic Hauton on 25/03/2016.
 * <p>
 * Tests for the TwitchMessage data class
 */
class TwitchMessageTest {
  private final String payload1 = "P1yloAd foobar 1!";
  private final String payload2 = "P1yloAd foobar 2!";
  private final String payload1Simple = "p1yloadfoobar1!";
  private final String payload2Simple = "p1yloadfoobar2!";
  private final String twitchUsername = "user1";
  private final String sourceChannel = "#foochannel";
  private final DateTime baseDateTime = DateTime.now();
  private TwitchMessage twitchMessagePayload1User1_1;
  private TwitchMessage twitchMessagePayload1User1_2;
  private TwitchMessage twitchMessagePayload2User1;

  @BeforeEach
  void setUp() throws Exception {
    TwitchUser twitchUser = new TwitchUser(twitchUsername);
    twitchMessagePayload1User1_1 = new TwitchMessage(payload1, twitchUser, baseDateTime, sourceChannel);
    twitchMessagePayload1User1_2 = new TwitchMessage(payload1, twitchUser, baseDateTime, sourceChannel);
    twitchMessagePayload2User1 = new TwitchMessage(payload2, twitchUser, baseDateTime, sourceChannel);
  }

  @Test
  void getMessagePayload() throws Exception {
    Assertions.assertEquals(payload1, twitchMessagePayload1User1_1.getMessage());
    Assertions.assertEquals(payload1, twitchMessagePayload1User1_2.getMessage());
    Assertions.assertEquals(payload2, twitchMessagePayload2User1.getMessage());
  }

  @Test
  void getSimpleMessagePayload() throws Exception {
    Assertions.assertEquals(payload1Simple, twitchMessagePayload1User1_1.getSimpleMessagePayload());
    Assertions.assertEquals(payload1Simple, twitchMessagePayload1User1_2.getSimpleMessagePayload());
    Assertions.assertEquals(payload2Simple, twitchMessagePayload2User1.getSimpleMessagePayload());
  }

  @Test
  void getSender() throws Exception {
    Assertions.assertEquals(twitchUsername, twitchMessagePayload1User1_1.getUsername());
  }

  @Test
  void getMessageDateTime() throws Exception {
    Assertions.assertEquals(baseDateTime, twitchMessagePayload1User1_1.getMessageDateTime());
  }

  @Test
  void getLegalCharRatio() throws Exception {
    Set<Character> legalChars = Stream.of('a', 'b').collect(Collectors.toSet());
    Assertions.assertEquals(
        3d / twitchMessagePayload1User1_1.getSimpleMessagePayload().length(),
        twitchMessagePayload1User1_1.getLegalCharRatio(legalChars),
        0.05d);
  }

  @Test
  void equalsSimplePayload() throws Exception {
    Assertions.assertTrue(twitchMessagePayload1User1_1.equalsSimplePayload(payload1Simple));
    Assertions.assertTrue(twitchMessagePayload1User1_1.equalsSimplePayload(payload1));
    Assertions.assertFalse(twitchMessagePayload1User1_1.equalsSimplePayload(payload2));
    Assertions.assertFalse(twitchMessagePayload1User1_1.equalsSimplePayload(payload2Simple));
  }

  @Test
  void containsStringTest() throws Exception {
    Assertions.assertTrue(twitchMessagePayload1User1_1.containsString(payload1.substring(5, 9)),
        "Should contain part of self");
    Assertions.assertTrue(twitchMessagePayload1User1_1.containsString(""),
        "Should contain empty message");
    Assertions.assertFalse(twitchMessagePayload1User1_1.containsString(payload2.substring(2) + "s"));
  }

  @Test
  void toStringTest() throws Exception {
    Assertions.assertTrue(twitchMessagePayload1User1_1.toString().contains(baseDateTime.toString()),
        "Should contain time");
    Assertions.assertTrue(twitchMessagePayload1User1_1.toString().contains(payload1),
        "Should contain payload");
    Assertions.assertTrue(twitchMessagePayload1User1_1.toString().contains(twitchUsername),
        "Should contain user");
  }

  @Test
  void hashCodeTest() throws Exception {
    Assertions.assertEquals(twitchMessagePayload1User1_1.hashCode(), twitchMessagePayload1User1_2.hashCode(),
        "Should have same hashcode!");
  }

  @Test
  void equalityTest() throws Exception {
    Assertions.assertTrue(twitchMessagePayload1User1_1.equals(twitchMessagePayload1User1_2),
        "Should have same hashcode!");
  }
}