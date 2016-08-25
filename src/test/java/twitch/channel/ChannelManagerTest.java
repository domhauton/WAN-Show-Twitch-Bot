package twitch.channel;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import twitch.channel.blacklist.BlacklistType;
import twitch.channel.message.TwitchMessage;
import twitch.channel.permissions.UserPermission;
import twitch.channel.settings.ChannelSettingDAOHashMapImpl;
import twitch.channel.timeouts.TimeoutReason;

import java.util.Collection;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by Dominic Hauton on 22/08/2016.
 * <p>
 * Main test for all channel manager methods.
 */
public class ChannelManagerTest {

    private ChannelManager m_channelManager;
    private TwitchUser m_twitchUser1;
    private TwitchMessage m_twitchMessage1;

    private static String s_channelName = "fooChannel";
    private static String s_payload1 = "foobarMessage1";

    @Before
    public void setUp() throws Exception {
        m_channelManager = new ChannelManager(new ChannelSettingDAOHashMapImpl(), s_channelName);
        m_twitchUser1 = new TwitchUser("foobarUser1");
        m_twitchMessage1 = new TwitchMessage(s_payload1, m_twitchUser1, DateTime.now(), s_channelName);
    }

    @Test
    public void permissionSimpleTest() throws Exception {
        m_channelManager.setPermission(m_twitchUser1, UserPermission.BotAdmin);
        Assert.assertFalse("Should not have permissions for ChannelOwner.", m_channelManager.checkPermission(m_twitchUser1, UserPermission.ChannelOwner));
        Assert.assertTrue("Should have permissions for BotAdmin.", m_channelManager.checkPermission(m_twitchUser1, UserPermission.BotAdmin));
        Assert.assertFalse("Should have permissions for BotAdmin.", m_channelManager.checkPermission(m_twitchUser1, UserPermission.ChannelOwner));
        Assert.assertFalse("Should have permissions for ChannelOwner.", m_channelManager.checkPermission(m_twitchUser1, UserPermission.ChannelOwner));
    }

    @Test
    public void permissionSetAndGetTest() throws Exception {
        Stream.of(UserPermission.values()).forEach(this::permissionsSetAndTestSingle);
    }

    private void permissionsSetAndTestSingle(UserPermission userPermission) {
        m_channelManager.setPermission(m_twitchUser1, userPermission);
        UserPermission actualPermission = m_channelManager.getPermission(m_twitchUser1);
        Assert.assertEquals("Value just set, should be identical", userPermission, actualPermission);
    }

    @Test
    public void messageManagerSimpleTest() throws Exception {
        m_channelManager.addChannelMessage(m_twitchMessage1);
        Assert.assertEquals("Should detect new message", 1, m_channelManager.getMessageSnapshot()
                .containsSimplePayload(s_payload1));
        Assert.assertEquals("Should detect new message", 1, m_channelManager.getMessageSnapshot(m_twitchUser1)
                .containsSimplePayload(s_payload1));
    }

    @Test
    public void messageManagerOverflowTest() throws Exception {
        // Insert very high number of messages.
        IntStream.range(1, 1000).boxed().forEach(x -> addChannelMessageUnsafe(m_twitchMessage1));
        Assert.assertEquals("Messages should be deleted after reaching cap. If fail, check cap has not changed!", 100, m_channelManager
                .getMessageSnapshot()
                .containsSimplePayload(s_payload1));
        Assert.assertEquals("Messages should be deleted after reaching cap. If fail, check cap has not changed!", 10, m_channelManager
                .getMessageSnapshot(m_twitchUser1)
                .containsSimplePayload(s_payload1));
    }

    /**
     * Casts exception to runtime. Only safe for tests
     */
    private void addChannelMessageUnsafe(TwitchMessage twitchMessage) {
        try {
            m_channelManager.addChannelMessage(twitchMessage);
        } catch (ChannelOperationException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    public void userTimeoutSimpleTest() throws Exception {
        Duration actualTimeout1 = m_channelManager.addUserTimeout(m_twitchUser1.getUsername(), TimeoutReason
                .MESSAGE_RATE);
        Assert.assertTrue("actualTimeout should be above zero", actualTimeout1.isLongerThan(Duration.ZERO));
        Duration actualTimeout2 = m_channelManager.addUserTimeout(m_twitchUser1.getUsername(), TimeoutReason
                .MESSAGE_RATE);
        Assert.assertTrue("actualTimeout should be above timeout1", actualTimeout2.isLongerThan(actualTimeout1));
        Duration actualTimeoutRetrieved = m_channelManager.getUserTimeout(m_twitchUser1);
        Assert.assertEquals("Returned and retrieved timeouts should be identical", actualTimeout2, actualTimeoutRetrieved);
    }

    @Test
    public void blackListItemSimpleTest() throws Exception {
        Collection<TwitchMessage> retroBannedMessages = m_channelManager.blacklistItem("foobar", BlacklistType.WORD);
        Assert.assertTrue("There should be no messages in the ban list yet!", retroBannedMessages.isEmpty());
        Assert.assertFalse("Message should be blacklisted!", m_channelManager.addChannelMessage(m_twitchMessage1));
        Collection<TwitchMessage> retroBannedMessages2 = m_channelManager.blacklistItem(s_payload1, BlacklistType.MESSAGE);
        Assert.assertFalse("The added message should be in the list.", retroBannedMessages2.isEmpty());
    }

    @Test
    public void blackListItemRetroactiveTest() throws Exception {
        int messagesToAdd = 10;
        IntStream.range(0, messagesToAdd).boxed().forEach(x -> addChannelMessageUnsafe(m_twitchMessage1));
        Collection<TwitchMessage> retroBannedMessages = m_channelManager.blacklistItem(".*foobar.*", BlacklistType
                .REGEX);
        Assert.assertEquals("All added messages should break rule", messagesToAdd, retroBannedMessages.size());
        Collection<TwitchMessage> retroBannedMessagesNone = m_channelManager.blacklistItem(".*foobar.*", BlacklistType
                .REGEX, 0);
        Assert.assertTrue("Ensure no retroactive bans.", retroBannedMessagesNone.isEmpty());
        Collection<TwitchMessage> retroBannedMessagesNeg = m_channelManager.blacklistItem(".*foobar.*", BlacklistType
                .REGEX, -1);
        Assert.assertTrue("Ensure no retroactive bans.", retroBannedMessagesNeg.isEmpty());
    }

    //TODO Blacklist removal tests.
}