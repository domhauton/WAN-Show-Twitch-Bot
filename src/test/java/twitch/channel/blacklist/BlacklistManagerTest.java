package twitch.channel.blacklist;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import twitch.channel.TwitchUser;
import twitch.channel.message.TwitchMessage;

import java.util.regex.Pattern;

/**
 * Created by Dominic Hauton on 13/07/2016.
 *
 * Tests the blacklist manager to see if messages are correctly blacklisted.
 */
public class BlacklistManagerTest {
    private static final String channelName = "channelfoobar";
    private static final TwitchUser mockUser = new TwitchUser("userfoobar");
    private static final TwitchMessage message1 = new TwitchMessage("foobar", mockUser, DateTime.now(), channelName);
    private static final TwitchMessage message2 = new TwitchMessage("fooba", mockUser, DateTime.now(), channelName);

    private BlacklistManager blacklistManager;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        blacklistManager = new BlacklistManager();
    }

    @Test
    public void addToBlacklistMessage() throws Exception {
        Assert.assertFalse("Message should not be blacklisted yet", blacklistManager.isMessageBlacklisted(message1.getMessage()));
        blacklistManager.addToBlacklist(message1.getMessage(), BlacklistType.MESSAGE);
        Assert.assertTrue("Message should now been blacklisted", blacklistManager.isMessageBlacklisted(message1.getMessage()));
        Assert.assertFalse("Message should not now been blacklisted", blacklistManager.isMessageBlacklisted(message2.getMessage()));
    }

    @Test
    public void addTwoMessagesToBlacklist() throws Exception {
        addToBlacklistMessage();
        blacklistManager.addToBlacklist(message2.getMessage(), BlacklistType.MESSAGE);
        Assert.assertTrue("Message should now been blacklisted", blacklistManager.isMessageBlacklisted(message2.getMessage()));
        Assert.assertTrue("Message 1 is should still be blacklisted", blacklistManager.isMessageBlacklisted(message1.getMessage()));
    }

    @Test
    public void removeMessageFromBlacklist() throws Exception {
        addTwoMessagesToBlacklist();
        blacklistManager.removeFromBlacklist(message2.getMessage(), BlacklistType.MESSAGE);
        Assert.assertFalse("Message 2 should no longer be blacklisted", blacklistManager.isMessageBlacklisted
                (message2.getMessage()));
        Assert.assertTrue("Message 1 should still blacklisted", blacklistManager.isMessageBlacklisted(message1.getMessage()));
    }

    @Test
    public void removeMessageFromBlacklistDoesNotExistException() throws Exception {
        addToBlacklistMessage();
        thrown.expect(BlacklistOperationOperationException.class);
        thrown.reportMissingExceptionWithMessage("Exception should be thrown. Should not be able to remove from "
                                                 + "blacklist.");
        blacklistManager.removeFromBlacklist(message2.getMessage(), BlacklistType.MESSAGE);
    }

}