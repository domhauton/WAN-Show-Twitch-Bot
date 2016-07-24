package twitch.channel.message;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import twitch.channel.TwitchUser;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.IntStream;

/**
 * Created by Dominic Hauton on 15/07/2016.
 *
 * Testing the message manager.
 */
public class MessageManagerTest {
    private final static String s_channel1 = "fooBarChannel1";
    private final static TwitchUser s_twitchUser1 = new TwitchUser("fooUser1");
    private final static TwitchUser s_twitchUser2 = new TwitchUser("fooUser2");
    private final static TwitchMessage s_twitchMessage1User1 = new TwitchMessage("foobar1", s_twitchUser1, DateTime.now(), s_channel1);
    private final static TwitchMessage s_twitchMessage2User1 = new TwitchMessage("foobar2", s_twitchUser1, DateTime.now(), s_channel1);
    private final static TwitchMessage s_twitchMessage1User2 = new TwitchMessage("foobar3", s_twitchUser2, DateTime.now(), s_channel1);
    private final static TwitchMessage s_twitchMessage2User2 = new TwitchMessage("foobar4", s_twitchUser2, DateTime.now(), s_channel1);

    private MessageManager messageManager;

    @Before
    public void setUp() {
        messageManager = new MessageManager(4, 10);
    }

    @Test
    public void addFirstMessageAndGetSnapshotTest() throws Exception {
        ImmutableTwitchMessageList channelSnapshotEmpty = messageManager.getChannelSnapshot();
        Assert.assertTrue("Should contain no messages", channelSnapshotEmpty.size() == 0);
        messageManager.addMessage(s_twitchMessage1User1);
        ImmutableTwitchMessageList channelSnapshotOneMessage = messageManager.getChannelSnapshot();
        Assert.assertTrue("Should only contain one message.", channelSnapshotOneMessage.size() == 1);
        ImmutableTwitchMessageList expectedImmutableMessageList = new ImmutableTwitchMessageList(Collections.singletonList(s_twitchMessage1User1));
        Assert.assertEquals("Actual immutableMessageList should be identical to the singleton list given. Correct "
                            + "size but incorrect message!", channelSnapshotOneMessage, expectedImmutableMessageList);
    }

    @Test
    public void addFirstFourMessagesAndGetSnapshotTest() throws Exception {
        putAllMessagesIntoMessageManager();
        ImmutableTwitchMessageList actualFullChannelSnapshot = messageManager.getChannelSnapshot();
        Collection<TwitchMessage> expectedTwitchMessageCollection = Arrays.asList(s_twitchMessage1User1, s_twitchMessage2User1, s_twitchMessage1User2, s_twitchMessage2User2);
        ImmutableTwitchMessageList expectedFullChannelSnapshot = new ImmutableTwitchMessageList(expectedTwitchMessageCollection);
        Assert.assertEquals("Messages should come out in correct order and match each other", expectedFullChannelSnapshot, actualFullChannelSnapshot);
    }

    @Test
    public void addFirstFourMessagesAndGetUserSnapshotTest() throws Exception {
        putAllMessagesIntoMessageManager();

        ImmutableTwitchMessageList actualUser1Snapshot = messageManager.getUserSnapshot(s_twitchUser1);
        Collection<TwitchMessage> expectedTwitchMessageCollectionUser1 = Arrays.asList(s_twitchMessage1User1, s_twitchMessage2User1);
        ImmutableTwitchMessageList expectedUser1Snapshot = new ImmutableTwitchMessageList(expectedTwitchMessageCollectionUser1);
        Assert.assertEquals("Only user 1 messages should be returned. In correct order.", expectedUser1Snapshot,
                actualUser1Snapshot);

        ImmutableTwitchMessageList actualUser2Snapshot = messageManager.getUserSnapshot(s_twitchUser2);
        Collection<TwitchMessage> expectedTwitchMessagesUser2 = Arrays.asList(s_twitchMessage1User2,
                s_twitchMessage2User2);
        ImmutableTwitchMessageList expectedUser2Snapshot = new ImmutableTwitchMessageList(expectedTwitchMessagesUser2);
        Assert.assertEquals("Only user 2 messages should be returned. In correct order.", expectedUser2Snapshot,
                actualUser2Snapshot);
    }

    @Test
    public void extractMessagesForEmptyUserTest() throws Exception {
        messageManager.addMessage(s_twitchMessage1User1);
        ImmutableTwitchMessageList userSnapshot = messageManager.getUserSnapshot(s_twitchUser2);
        Assert.assertTrue("There should be no messages for given user", userSnapshot.size() == 0);
    }

    @Test
    public void extractMessagesForEmptyChannelSnapshot() throws Exception {
        ImmutableTwitchMessageList actualEmptySnapshot = messageManager.getChannelSnapshot();
        ImmutableTwitchMessageList expectedEmptySnaphot = new ImmutableTwitchMessageList(Collections.emptyList());
        Assert.assertEquals("Should be able to return empty snapshot", expectedEmptySnaphot, actualEmptySnapshot);
    }

    @Test
    public void overflowBufferTest() throws Exception {
        IntStream.range(0, 12).forEach(x -> messageManager.addMessage(s_twitchMessage1User1));
        ImmutableTwitchMessageList userSnapshot = messageManager.getUserSnapshot(s_twitchUser1);
        ImmutableTwitchMessageList channelSnapshot = messageManager.getChannelSnapshot();
        Assert.assertTrue("User snapshot should be of size 4.", userSnapshot.size() == 4);
        Assert.assertTrue("Channel snapshot should be of size 10.", channelSnapshot.size() == 10);
    }

    private void putAllMessagesIntoMessageManager() {
        messageManager.addMessage(s_twitchMessage1User1);
        messageManager.addMessage(s_twitchMessage2User1);
        messageManager.addMessage(s_twitchMessage1User2);
        messageManager.addMessage(s_twitchMessage2User2);
    }
}