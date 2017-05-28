package bot.channel.message;

import bot.channel.TwitchUser;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.IntStream;

/**
 * Created by Dominic Hauton on 15/07/2016.
 * <p>
 * Testing the message manager.
 */

class MessageManagerTest {
  private final static String channel1 = "fooBarChannel1";
  private final static TwitchUser twitchUser1 = new TwitchUser("fooUser1");
  private final static TwitchUser twitchUser2 = new TwitchUser("fooUser2");
  private final static TwitchMessage twitchMessage1User1 = new TwitchMessage("foobar1", twitchUser1, DateTime.now(), channel1);
  private final static TwitchMessage twitchMessage2User1 = new TwitchMessage("foobar2", twitchUser1, DateTime.now(), channel1);
  private final static TwitchMessage twitchMessage1User2 = new TwitchMessage("foobar3", twitchUser2, DateTime.now(), channel1);
  private final static TwitchMessage twitchMessage2User2 = new TwitchMessage("foobar4", twitchUser2, DateTime.now(), channel1);

  private MessageManager messageManager;

  @BeforeEach
  void setUp() {
    messageManager = new MessageManager(4, 10);
  }

  @Test
  void addFirstMessageAndGetSnapshotTest() throws Exception {
    ImmutableTwitchMessageList channelSnapshotEmpty = messageManager.getChannelSnapshot();
    Assertions.assertTrue(channelSnapshotEmpty.size() == 0, "Should contain no messages");
    messageManager.addMessage(twitchMessage1User1);
    ImmutableTwitchMessageList channelSnapshotOneMessage = messageManager.getChannelSnapshot();
    Assertions.assertTrue(channelSnapshotOneMessage.size() == 1, "Should only contain one message.");
    ImmutableTwitchMessageList expectedImmutableMessageList = new ImmutableTwitchMessageList(Collections.singletonList(twitchMessage1User1));
    Assertions.assertEquals(channelSnapshotOneMessage, expectedImmutableMessageList,
        "Actual immutableMessageList should be identical to the singleton list given. Correct "
            + "size but incorrect message!");
  }

  @Test
  void addFirstFourMessagesAndGetSnapshotTest() throws Exception {
    putAllMessagesIntoMessageManager();
    ImmutableTwitchMessageList actualFullChannelSnapshot = messageManager.getChannelSnapshot();
    Collection<TwitchMessage> expectedTwitchMessageCollection = Arrays.asList(twitchMessage1User1, twitchMessage2User1, twitchMessage1User2, twitchMessage2User2);
    ImmutableTwitchMessageList expectedFullChannelSnapshot = new ImmutableTwitchMessageList(expectedTwitchMessageCollection);
    Assertions.assertEquals(expectedFullChannelSnapshot, actualFullChannelSnapshot,
        "Messages should come out in correct order and match each other");
  }

  @Test
  void addFirstFourMessagesAndGetUserSnapshotTest() throws Exception {
    putAllMessagesIntoMessageManager();

    ImmutableTwitchMessageList actualUser1Snapshot = messageManager.getUserSnapshot(twitchUser1);
    Collection<TwitchMessage> expectedTwitchMessageCollectionUser1 = Arrays.asList(twitchMessage1User1, twitchMessage2User1);
    ImmutableTwitchMessageList expectedUser1Snapshot = new ImmutableTwitchMessageList(expectedTwitchMessageCollectionUser1);
    Assertions.assertEquals(expectedUser1Snapshot, actualUser1Snapshot,
        "Only user 1 messages should be returned. In correct order.");

    ImmutableTwitchMessageList actualUser2Snapshot = messageManager.getUserSnapshot(twitchUser2);
    Collection<TwitchMessage> expectedTwitchMessagesUser2 = Arrays.asList(twitchMessage1User2,
        twitchMessage2User2);
    ImmutableTwitchMessageList expectedUser2Snapshot = new ImmutableTwitchMessageList(expectedTwitchMessagesUser2);
    Assertions.assertEquals(expectedUser2Snapshot, actualUser2Snapshot,
        "Only user 2 messages should be returned. In correct order.");
  }

  @Test
  void extractMessagesForEmptyUserTest() throws Exception {
    messageManager.addMessage(twitchMessage1User1);
    ImmutableTwitchMessageList userSnapshot = messageManager.getUserSnapshot(twitchUser2);
    Assertions.assertTrue(userSnapshot.size() == 0, "There should be no messages for given user");
  }

  @Test
  void extractMessagesForEmptyChannelSnapshot() throws Exception {
    ImmutableTwitchMessageList actualEmptySnapshot = messageManager.getChannelSnapshot();
    ImmutableTwitchMessageList expectedEmptySnaphot = new ImmutableTwitchMessageList(Collections.emptyList());
    Assertions.assertEquals(expectedEmptySnaphot, actualEmptySnapshot, "Should be able to return empty snapshot");
  }

  @Test
  void overflowBufferTest() throws Exception {
    IntStream.range(0, 12).forEach(x -> messageManager.addMessage(twitchMessage1User1));
    ImmutableTwitchMessageList userSnapshot = messageManager.getUserSnapshot(twitchUser1);
    ImmutableTwitchMessageList channelSnapshot = messageManager.getChannelSnapshot();
    Assertions.assertTrue(userSnapshot.size() == 4, "User snapshot should be of size 4.");
    Assertions.assertTrue(channelSnapshot.size() == 10, "Channel snapshot should be of size 10.");
  }

  private void putAllMessagesIntoMessageManager() {
    messageManager.addMessage(twitchMessage1User1);
    messageManager.addMessage(twitchMessage2User1);
    messageManager.addMessage(twitchMessage1User2);
    messageManager.addMessage(twitchMessage2User2);
  }
}