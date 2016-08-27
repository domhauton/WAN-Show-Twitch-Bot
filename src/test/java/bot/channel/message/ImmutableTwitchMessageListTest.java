package bot.channel.message;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import bot.channel.TwitchUser;

/**
 * Created by Dominic Hauton on 19/03/2016.
 *
 * Test for all functions in ImmutableTwitchMessageList
 */
public class ImmutableTwitchMessageListTest {

  private static final String user1 = "user1";
  private static final String user2 = "user2";

  private static final String payload1 = "msg1";

  private static final String sourceChannel = "#foochannel";

  private TwitchMessage twitchMessage1;
  private TwitchMessage twitchMessage2;
  private TwitchMessage twitchMessage3;
  private TwitchMessage twitchMessage4;

  @Before
  public void setUp() {
    DateTime dateTime = DateTime.now();
    TwitchUser twitchUser1 = new TwitchUser(user1);
    TwitchUser twitchUser2 = new TwitchUser(user2);
    twitchMessage1 = new TwitchMessage(payload1, twitchUser1, dateTime, sourceChannel);
    twitchMessage2 = new TwitchMessage("msg2", twitchUser2, dateTime.plusSeconds(1), sourceChannel);
    twitchMessage3 = new TwitchMessage("msg3", twitchUser1, dateTime.plusSeconds(2), sourceChannel);
    twitchMessage4 = new TwitchMessage("msg4", twitchUser2, dateTime.plusSeconds(3), sourceChannel);
  }

  @Test
  public void testUserFilteringRemoveHalf() {
    Collection<TwitchMessage> originalTwitchMessages =
        Arrays.asList(twitchMessage1, twitchMessage2, twitchMessage3, twitchMessage4);

    Collection<TwitchMessage> expectedMessages = Arrays.asList(twitchMessage1, twitchMessage3);

    ImmutableTwitchMessageList immutableMessageList = new ImmutableTwitchMessageList(originalTwitchMessages);
    ImmutableTwitchMessageList actualFilteredMessageList = immutableMessageList.filterUser(new TwitchUser(user1));
    ImmutableTwitchMessageList expectedFilteredTwitchMessages = new ImmutableTwitchMessageList(expectedMessages);

    Assert.assertEquals("Filtered out user1 messages", expectedFilteredTwitchMessages, actualFilteredMessageList);
  }

  @Test
  public void testUserFilteringRemoveNone() {
    Collection<TwitchMessage> originalTwitchMessages =
        Arrays.asList(twitchMessage2, twitchMessage4);

    Collection<TwitchMessage> expectedMessages = Arrays.asList(twitchMessage2, twitchMessage4);

    ImmutableTwitchMessageList immutableMessageList = new ImmutableTwitchMessageList(originalTwitchMessages);
    ImmutableTwitchMessageList actualFilteredMessageList = immutableMessageList.filterUser(new TwitchUser(user2));
    ImmutableTwitchMessageList expectedFilteredTwitchMessages = new ImmutableTwitchMessageList(expectedMessages);

    Assert.assertEquals("Filtered out no messages", expectedFilteredTwitchMessages, actualFilteredMessageList);
  }

  @Test
  public void testUserFilteringRemoveAll() {
    Collection<TwitchMessage> originalTwitchMessages =
        Arrays.asList(twitchMessage2, twitchMessage4);

    ImmutableTwitchMessageList immutableMessageList = new ImmutableTwitchMessageList(originalTwitchMessages);
    ImmutableTwitchMessageList actualFilteredMessageList = immutableMessageList.filterUser(new TwitchUser(user1));
    ImmutableTwitchMessageList expectedFilteredTwitchMessages = new ImmutableTwitchMessageList(new HashSet<>());

    Assert.assertEquals("Filtered out no messages", expectedFilteredTwitchMessages, actualFilteredMessageList);
  }

  @Test
  public void testImmutableMessageListGivenNull() {
    ImmutableTwitchMessageList twitchMessageList = new ImmutableTwitchMessageList(null);
    Assert.assertEquals("Ensure list is empty.", twitchMessageList.size(), 0);
  }

  @Test
  public void testSize() {
    Collection<TwitchMessage> twitchMessages = Arrays.asList(twitchMessage1, twitchMessage3);
    ImmutableTwitchMessageList twitchMessageList = new ImmutableTwitchMessageList(twitchMessages);
    Assert.assertEquals("Ensure counts both messages.", twitchMessageList.size(), 2);
  }

  @Test
  public void testTimeSpanNoMessages() {
    ImmutableTwitchMessageList twitchMessageList = new ImmutableTwitchMessageList(null);
    Assert.assertEquals("Ensure period zero is given if empty", twitchMessageList.getMessageTimePeriod(), Period.ZERO);
  }

  @Test
  public void testTimeSpanTwoMessages() {
    Collection<TwitchMessage> twitchMessages = Arrays.asList(twitchMessage1, twitchMessage2);
    ImmutableTwitchMessageList twitchMessageList = new ImmutableTwitchMessageList(twitchMessages);
    Assert.assertEquals("Ensure period zero is given if empty", twitchMessageList.getMessageTimePeriod(), new Period(0, 0, 1, 0));
  }

  @Test
  public void testTimeSpanThreeMessages() {
    Collection<TwitchMessage> twitchMessages = Arrays.asList(twitchMessage1, twitchMessage2, twitchMessage4);
    ImmutableTwitchMessageList twitchMessageList = new ImmutableTwitchMessageList(twitchMessages);
    Assert.assertEquals("Ensure period zero is given if empty", twitchMessageList.getMessageTimePeriod(), new Period(0, 0, 3, 0));
  }

  @Test
  public void stream() {
    Collection<TwitchMessage> twitchMessages = Arrays.asList(twitchMessage1, twitchMessage2, twitchMessage4);
    ImmutableTwitchMessageList twitchMessageList = new ImmutableTwitchMessageList(twitchMessages);
    Assert.assertEquals("Ensure stream returned has correct count", twitchMessageList.stream().count(), twitchMessageList.size());
  }

  @Test
  public void containsSimplePayloadTrueSimple() {
    Collection<TwitchMessage> twitchMessages = Arrays.asList(twitchMessage1, twitchMessage2, twitchMessage4);
    ImmutableTwitchMessageList twitchMessageList = new ImmutableTwitchMessageList(twitchMessages);
    Assert.assertTrue("Assert MESSAGE is found correctly.", twitchMessageList.containsSimplePayload(payload1) >= 1);
  }

  @Test
  public void containsSimplePayloadTrueSpaces() {
    Collection<TwitchMessage> twitchMessages = Arrays.asList(twitchMessage1, twitchMessage2, twitchMessage4);
    ImmutableTwitchMessageList twitchMessageList = new ImmutableTwitchMessageList(twitchMessages);
    Assert.assertTrue("Assert MESSAGE is found correctly.", twitchMessageList.containsSimplePayload(payload1 + " ") >= 1);
  }

  @Test
  public void containsSimplePayloadTrueUpperCase() {
    Collection<TwitchMessage> twitchMessages = Arrays.asList(twitchMessage1, twitchMessage2, twitchMessage4);
    ImmutableTwitchMessageList twitchMessageList = new ImmutableTwitchMessageList(twitchMessages);
    Assert.assertTrue("Assert MESSAGE is found correctly.", twitchMessageList.containsSimplePayload(payload1.toUpperCase()) >= 1);
  }

  @Test
  public void containsSimplePayloadTrueUpperCaseAndSpace() {
    Collection<TwitchMessage> twitchMessages = Arrays.asList(twitchMessage1, twitchMessage2, twitchMessage4);
    ImmutableTwitchMessageList twitchMessageList = new ImmutableTwitchMessageList(twitchMessages);
    Assert.assertTrue("Assert MESSAGE is found correctly.", twitchMessageList.containsSimplePayload(payload1.toUpperCase() + " ") >= 1);
  }

  @Test
  public void containsSimplePayloadFalse() {
    Collection<TwitchMessage> twitchMessages = Arrays.asList(twitchMessage1, twitchMessage2, twitchMessage4);
    ImmutableTwitchMessageList twitchMessageList = new ImmutableTwitchMessageList(twitchMessages);
    Assert.assertFalse("Assert MESSAGE is found correctly.", twitchMessageList.containsSimplePayload(payload1 + "foobar") >= 1);
  }

  @Test
  public void hashCodeTest() throws Exception {
    Collection<TwitchMessage> twitchMessages = Arrays.asList(twitchMessage1, twitchMessage2, twitchMessage4);
    ImmutableTwitchMessageList twitchMessageList1 = new ImmutableTwitchMessageList(twitchMessages);
    ImmutableTwitchMessageList twitchMessageList2 = new ImmutableTwitchMessageList(twitchMessages);
    Assert.assertEquals("Hashcodes should be the same", twitchMessageList1.hashCode(), twitchMessageList2.hashCode());
  }

  @Test
  public void testToString() throws Exception {
    Collection<TwitchMessage> twitchMessages = Arrays.asList(twitchMessage1, twitchMessage2, twitchMessage4);
    ImmutableTwitchMessageList twitchMessageList1 = new ImmutableTwitchMessageList(twitchMessages);
    Assert.assertTrue("List should contain at least one of the added messages", twitchMessageList1.toString()
        .contains(twitchMessage1.toString()));
  }
}