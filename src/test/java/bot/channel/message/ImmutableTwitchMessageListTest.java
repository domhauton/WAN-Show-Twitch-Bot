package bot.channel.message;

import bot.channel.TwitchUser;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by Dominic Hauton on 19/03/2016.
 * <p>
 * Test for all functions in ImmutableTwitchMessageList
 */
class ImmutableTwitchMessageListTest {

  private static final String user1 = "user1";
  private static final String user2 = "user2";

  private static final String payload1 = "msg1";

  private static final String sourceChannel = "#foochannel";

  private TwitchMessage twitchMessage1;
  private TwitchMessage twitchMessage2;
  private TwitchMessage twitchMessage3;
  private TwitchMessage twitchMessage4;

  @BeforeEach
  void setUp() {
    DateTime dateTime = DateTime.now();
    TwitchUser twitchUser1 = new TwitchUser(user1);
    TwitchUser twitchUser2 = new TwitchUser(user2);
    twitchMessage1 = new TwitchMessage(payload1, twitchUser1, dateTime, sourceChannel);
    twitchMessage2 = new TwitchMessage("msg2", twitchUser2, dateTime.plusSeconds(1), sourceChannel);
    twitchMessage3 = new TwitchMessage("msg3", twitchUser1, dateTime.plusSeconds(2), sourceChannel);
    twitchMessage4 = new TwitchMessage("msg4", twitchUser2, dateTime.plusSeconds(3), sourceChannel);
  }

  @Test
  void testUserFilteringRemoveHalf() {
    Collection<TwitchMessage> originalTwitchMessages =
        Arrays.asList(twitchMessage1, twitchMessage2, twitchMessage3, twitchMessage4);

    Collection<TwitchMessage> expectedMessages = Arrays.asList(twitchMessage1, twitchMessage3);

    ImmutableTwitchMessageList immutableMessageList = new ImmutableTwitchMessageList(originalTwitchMessages);
    ImmutableTwitchMessageList actualFilteredMessageList = immutableMessageList.filterUser(new TwitchUser(user1));
    ImmutableTwitchMessageList expectedFilteredTwitchMessages = new ImmutableTwitchMessageList(expectedMessages);

    Assertions.assertEquals(expectedFilteredTwitchMessages, actualFilteredMessageList, "Filtered out user1 messages");
  }

  @Test
  void testUserFilteringRemoveNone() {
    Collection<TwitchMessage> originalTwitchMessages =
        Arrays.asList(twitchMessage2, twitchMessage4);

    Collection<TwitchMessage> expectedMessages = Arrays.asList(twitchMessage2, twitchMessage4);

    ImmutableTwitchMessageList immutableMessageList = new ImmutableTwitchMessageList(originalTwitchMessages);
    ImmutableTwitchMessageList actualFilteredMessageList = immutableMessageList.filterUser(new TwitchUser(user2));
    ImmutableTwitchMessageList expectedFilteredTwitchMessages = new ImmutableTwitchMessageList(expectedMessages);

    Assertions.assertEquals(expectedFilteredTwitchMessages, actualFilteredMessageList, "Filtered out no messages");
  }

  @Test
  void testUserFilteringRemoveAll() {
    Collection<TwitchMessage> originalTwitchMessages =
        Arrays.asList(twitchMessage2, twitchMessage4);

    ImmutableTwitchMessageList immutableMessageList = new ImmutableTwitchMessageList(originalTwitchMessages);
    ImmutableTwitchMessageList actualFilteredMessageList = immutableMessageList.filterUser(new TwitchUser(user1));
    ImmutableTwitchMessageList expectedFilteredTwitchMessages = new ImmutableTwitchMessageList(new HashSet<>());

    Assertions.assertEquals(expectedFilteredTwitchMessages, actualFilteredMessageList, "Filtered out no messages");
  }

  @Test
  void testImmutableMessageListGivenNull() {
    ImmutableTwitchMessageList twitchMessageList = new ImmutableTwitchMessageList(null);
    Assertions.assertEquals(twitchMessageList.size(), 0, "Ensure list is empty.");
  }

  @Test
  void testSize() {
    Collection<TwitchMessage> twitchMessages = Arrays.asList(twitchMessage1, twitchMessage3);
    ImmutableTwitchMessageList twitchMessageList = new ImmutableTwitchMessageList(twitchMessages);
    Assertions.assertEquals(twitchMessageList.size(), 2, "Ensure counts both messages.");
  }

  @Test
  void testTimeSpanNoMessages() {
    ImmutableTwitchMessageList twitchMessageList = new ImmutableTwitchMessageList(null);
    Assertions.assertEquals(twitchMessageList.getMessageTimePeriod(), Period.ZERO, "Ensure period zero is given if empty");
  }

  @Test
  void testTimeSpanTwoMessages() {
    Collection<TwitchMessage> twitchMessages = Arrays.asList(twitchMessage1, twitchMessage2);
    ImmutableTwitchMessageList twitchMessageList = new ImmutableTwitchMessageList(twitchMessages);
    Assertions.assertEquals(twitchMessageList.getMessageTimePeriod(), new Period(0, 0, 1, 0),
        "Ensure period zero is given if empty");
  }

  @Test
  void testTimeSpanThreeMessages() {
    Collection<TwitchMessage> twitchMessages = Arrays.asList(twitchMessage1, twitchMessage2, twitchMessage4);
    ImmutableTwitchMessageList twitchMessageList = new ImmutableTwitchMessageList(twitchMessages);
    Assertions.assertEquals(twitchMessageList.getMessageTimePeriod(), new Period(0, 0, 3, 0),
        "Ensure period zero is given if empty");
  }

  @Test
  void stream() {
    Collection<TwitchMessage> twitchMessages = Arrays.asList(twitchMessage1, twitchMessage2, twitchMessage4);
    ImmutableTwitchMessageList twitchMessageList = new ImmutableTwitchMessageList(twitchMessages);
    Assertions.assertEquals(twitchMessageList.stream().count(), twitchMessageList.size(), "Ensure stream returned has correct count");
  }

  @Test
  void containsSimplePayloadTrueSimple() {
    Collection<TwitchMessage> twitchMessages = Arrays.asList(twitchMessage1, twitchMessage2, twitchMessage4);
    ImmutableTwitchMessageList twitchMessageList = new ImmutableTwitchMessageList(twitchMessages);
    Assertions.assertTrue(twitchMessageList.containsSimplePayload(payload1) >= 1, "Assert MESSAGE is found correctly.");
  }

  @Test
  void containsSimplePayloadTrueSpaces() {
    Collection<TwitchMessage> twitchMessages = Arrays.asList(twitchMessage1, twitchMessage2, twitchMessage4);
    ImmutableTwitchMessageList twitchMessageList = new ImmutableTwitchMessageList(twitchMessages);
    Assertions.assertTrue(twitchMessageList.containsSimplePayload(payload1 + " ") >= 1, "Assert MESSAGE is found correctly.");
  }

  @Test
  void containsSimplePayloadTrueUpperCase() {
    Collection<TwitchMessage> twitchMessages = Arrays.asList(twitchMessage1, twitchMessage2, twitchMessage4);
    ImmutableTwitchMessageList twitchMessageList = new ImmutableTwitchMessageList(twitchMessages);
    Assertions.assertTrue(twitchMessageList.containsSimplePayload(payload1.toUpperCase()) >= 1, "Assert MESSAGE is found correctly.");
  }

  @Test
  void containsSimplePayloadTrueUpperCaseAndSpace() {
    Collection<TwitchMessage> twitchMessages = Arrays.asList(twitchMessage1, twitchMessage2, twitchMessage4);
    ImmutableTwitchMessageList twitchMessageList = new ImmutableTwitchMessageList(twitchMessages);
    Assertions.assertTrue(twitchMessageList.containsSimplePayload(payload1.toUpperCase() + " ") >= 1, "Assert MESSAGE is found correctly.");
  }

  @Test
  void containsSimplePayloadFalse() {
    Collection<TwitchMessage> twitchMessages = Arrays.asList(twitchMessage1, twitchMessage2, twitchMessage4);
    ImmutableTwitchMessageList twitchMessageList = new ImmutableTwitchMessageList(twitchMessages);
    Assertions.assertFalse(twitchMessageList.containsSimplePayload(payload1 + "foobar") >= 1, "Assert MESSAGE is found correctly.");
  }

  @Test
  void hashCodeTest() throws Exception {
    Collection<TwitchMessage> twitchMessages = Arrays.asList(twitchMessage1, twitchMessage2, twitchMessage4);
    ImmutableTwitchMessageList twitchMessageList1 = new ImmutableTwitchMessageList(twitchMessages);
    ImmutableTwitchMessageList twitchMessageList2 = new ImmutableTwitchMessageList(twitchMessages);
    Assertions.assertEquals(twitchMessageList1.hashCode(), twitchMessageList2.hashCode(), "Hashcodes should be the same");
  }

  @Test
  void testToString() throws Exception {
    Collection<TwitchMessage> twitchMessages = Arrays.asList(twitchMessage1, twitchMessage2, twitchMessage4);
    ImmutableTwitchMessageList twitchMessageList1 = new ImmutableTwitchMessageList(twitchMessages);
    Assertions.assertTrue(twitchMessageList1.toString().contains(twitchMessage1.toString()),
        "List should contain at least one of the added messages");
  }
}