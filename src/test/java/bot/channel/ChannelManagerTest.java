package bot.channel;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import bot.channel.blacklist.BlacklistEntry;
import bot.channel.blacklist.BlacklistManager;
import bot.channel.blacklist.BlacklistType;
import bot.channel.message.MessageManager;
import bot.channel.message.TwitchMessage;
import bot.channel.permissions.PermissionsManager;
import bot.channel.permissions.UserPermission;
import bot.channel.settings.ChannelSettingDAOHashMapImpl;
import bot.channel.settings.enums.ChannelSettingString;
import bot.channel.timeouts.TimeoutManager;
import bot.channel.timeouts.TimeoutReason;
import bot.channel.url.URLConverter;
import bot.channel.url.URLConverterImpl;
import bot.channel.url.URLInvalidException;

/**
 * Created by Dominic Hauton on 22/08/2016.
 * <p>
 * Main test for all channel manager methods.
 */
public class ChannelManagerTest {

  private static String channelName = "fooChannel";
  private static String payload1 = "foobarMessage1";
  @Rule
  public final ExpectedException expectedException = ExpectedException.none();
  private ChannelManager channelManager;
  private TwitchUser twitchUser1;
  private TwitchMessage twitchMessage1;

  @Before
  public void setUp() throws Exception {
    channelManager = new ChannelManager(channelName);
    twitchUser1 = new TwitchUser("foobarUser1");
    twitchMessage1 = new TwitchMessage(payload1, twitchUser1, DateTime.now(), channelName);
  }

  @Test
  public void permissionSimpleTest() throws Exception {
    channelManager.setPermission(twitchUser1, UserPermission.BotAdmin);
    Assert.assertFalse("Should not have permissions for ChannelOwner.", channelManager.checkPermission(twitchUser1, UserPermission.ChannelOwner));
    Assert.assertTrue("Should have permissions for BotAdmin.", channelManager.checkPermission(twitchUser1, UserPermission.BotAdmin));
    Assert.assertFalse("Should have permissions for BotAdmin.", channelManager.checkPermission(twitchUser1, UserPermission.ChannelOwner));
    Assert.assertFalse("Should have permissions for ChannelOwner.", channelManager.checkPermission(twitchUser1, UserPermission.ChannelOwner));
  }

  @Test
  public void permissionSetAndGetTest() throws Exception {
    Stream.of(UserPermission.values()).forEach(this::permissionsSetAndTestSingle);
  }

  private void permissionsSetAndTestSingle(UserPermission userPermission) {
    channelManager.setPermission(twitchUser1, userPermission);
    UserPermission actualPermission = null;
    try {
      actualPermission = channelManager.getPermission(twitchUser1);
    } catch (ChannelOperationException e) {
      Assert.fail(e.getMessage());
    }
    Assert.assertEquals("Value just set, should be identical", userPermission, actualPermission);
  }

  @Test
  public void permissionGetWithoutSetTest() throws Exception {
    UserPermission permission = channelManager.getPermission(twitchUser1);
    Assert.assertNotNull("Should have fetched default", permission);
  }

  @Test
  public void permissionGetWithoutSetFail() throws Exception {
    ChannelSettingDAOHashMapImpl mockChannelSettingDAO = Mockito.mock(ChannelSettingDAOHashMapImpl.class);
    Mockito.when(mockChannelSettingDAO.getSettingOrDefault(channelName, ChannelSettingString.DEFAULT_PERMISSION))
        .thenReturn("thispermissionwillnotexist");
    ChannelManager tempChannelManager = new ChannelManager(
        channelName,
        new PermissionsManager(),
        new MessageManager(),
        new TimeoutManager(),
        new BlacklistManager(),
        mockChannelSettingDAO,
        new URLConverterImpl());
    expectedException.expect(ChannelOperationException.class);
    tempChannelManager.getPermission(twitchUser1);
  }

  @Test
  public void messageManagerSimpleTest() throws Exception {
    channelManager.addChannelMessage(twitchMessage1);
    Assert.assertEquals("Should detect new message", 1, channelManager.getMessageSnapshot()
        .containsSimplePayload(payload1));
    Assert.assertEquals("Should detect new message", 1, channelManager.getMessageSnapshot(twitchUser1)
        .containsSimplePayload(payload1));
  }

  @Test
  public void messageManagerOverflowTest() throws Exception {
    // Insert very high number of messages.
    IntStream.range(1, 1000).boxed().forEach(x -> addChannelMessageUnsafe(twitchMessage1));
    Assert.assertEquals("Messages should be deleted after reaching cap. If fail, check cap has not changed!", 100, channelManager
        .getMessageSnapshot()
        .containsSimplePayload(payload1));
    Assert.assertEquals("Messages should be deleted after reaching cap. If fail, check cap has not changed!", 10, channelManager
        .getMessageSnapshot(twitchUser1)
        .containsSimplePayload(payload1));
  }

  @Test
  public void failMessageInsertTest() throws Exception {
    MessageManager mockMessageManager = Mockito.mock(MessageManager.class);
    TwitchMessage twitchMessage = new TwitchMessage("foobar", twitchUser1, DateTime.now(), channelName);
    Mockito.when(mockMessageManager.addMessage(twitchMessage)).thenReturn(false);
    ChannelManager tempChannelManager = new ChannelManager(
        channelName,
        new PermissionsManager(),
        mockMessageManager,
        new TimeoutManager(),
        new BlacklistManager(),
        new ChannelSettingDAOHashMapImpl(),
        new URLConverterImpl());
    expectedException.expect(ChannelOperationException.class);
    tempChannelManager.addChannelMessage(twitchMessage);
  }

  /**
   * Casts exception to runtime. Only safe for tests
   */
  private void addChannelMessageUnsafe(TwitchMessage twitchMessage) {
    try {
      channelManager.addChannelMessage(twitchMessage);
    } catch (ChannelOperationException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void userTimeoutSimpleTest() throws Exception {
    Duration actualTimeout1 = channelManager.addUserTimeout(twitchUser1.getUsername(), TimeoutReason
        .MESSAGE_RATE);
    Assert.assertTrue("actualTimeout should be above zero", actualTimeout1.isLongerThan(Duration.ZERO));
    Duration actualTimeout2 = channelManager.addUserTimeout(twitchUser1.getUsername(), TimeoutReason
        .MESSAGE_RATE);
    Assert.assertTrue("actualTimeout should be above timeout1", actualTimeout2.isLongerThan(actualTimeout1));
    Duration actualTimeoutRetrieved = channelManager.getUserTimeout(twitchUser1);
    Assert.assertEquals("Returned and retrieved timeouts should be identical", actualTimeout2, actualTimeoutRetrieved);
  }

  @Test
  public void blackListItemSimpleTest() throws Exception {
    Collection<TwitchMessage> retroBannedMessages = channelManager.blacklistItem("foobar", BlacklistType.WORD);
    Assert.assertTrue("There should be no messages in the ban list yet!", retroBannedMessages.isEmpty());
    Assert.assertFalse("Message should be blacklisted!", channelManager.addChannelMessage(twitchMessage1));
    Collection<TwitchMessage> retroBannedMessages2 = channelManager.blacklistItem(payload1, BlacklistType.MESSAGE);
    Assert.assertFalse("The added message should be in the list.", retroBannedMessages2.isEmpty());
  }

  @Test
  public void blackListItemRetroactiveTest() throws Exception {
    String regexToBlacklist = ".*foobar.*";
    int messagesToAdd = 10;
    IntStream.range(0, messagesToAdd).boxed().forEach(x -> addChannelMessageUnsafe(twitchMessage1));
    Collection<TwitchMessage> retroBannedMessages = channelManager.blacklistItem(regexToBlacklist, BlacklistType
        .REGEX);
    Assert.assertEquals("All added messages should break rule", messagesToAdd, retroBannedMessages.size());
    channelManager.removeBlacklistItem(regexToBlacklist, BlacklistType.REGEX);
    Collection<TwitchMessage> retroBannedMessagesNone = channelManager.blacklistItem(regexToBlacklist, BlacklistType
        .REGEX, 0);
    Assert.assertTrue("Ensure no retroactive bans.", retroBannedMessagesNone.isEmpty());
    channelManager.removeBlacklistItem(regexToBlacklist, BlacklistType.REGEX);
    Collection<TwitchMessage> retroBannedMessagesNeg = channelManager.blacklistItem(regexToBlacklist, BlacklistType
        .REGEX, -1);
    Assert.assertTrue("Ensure no retroactive bans.", retroBannedMessagesNeg.isEmpty());
  }

  @Test
  public void blackListRemoveItemTest() throws Exception {
    String finalTestMessage = "A final test for item 3";
    channelManager.blacklistItem("Test Item 1", BlacklistType.MESSAGE);
    channelManager.blacklistItem("A test for Item 2", BlacklistType.WORD);
    channelManager.blacklistItem(finalTestMessage, BlacklistType.REGEX);
    Collection<BlacklistEntry> blacklistEntries = channelManager.removeBlacklistItem("Item");
    Assert.assertEquals("Should have removed two entries", 2, blacklistEntries.size());
    TwitchMessage twitchMessage = new TwitchMessage(finalTestMessage, twitchUser1, DateTime.now(), channelName);
    Assert.assertFalse("Should not have removed mis-capitalised entry", channelManager.addChannelMessage(twitchMessage));
  }

  @Test
  public void blackListItemRemoveFail() throws Exception {
    expectedException.expect(ChannelOperationException.class);
    channelManager.removeBlacklistItem("foobar", BlacklistType.REGEX);
  }

  @Test
  public void blackListItemAddTwiceTest() throws Exception {
    channelManager.blacklistItem("foobar1", BlacklistType.REGEX);
    expectedException.expect(ChannelOperationException.class);
    channelManager.blacklistItem("foobar1", BlacklistType.REGEX);
  }

  @Test
  public void getChannelNameTest() throws Exception {
    String actualChannelName = channelManager.getChannelName();
    Assert.assertEquals(actualChannelName, channelName);
  }

  @Test
  public void customiseURLTest() throws Exception {
    String exampleString = "foobar";
    String actualString = channelManager.customiseURL(exampleString);
    Assert.assertEquals(exampleString, actualString);
  }

  @Test
  public void customiseURLFailTest() throws Exception {
    String exampleString = "foobar";
    URLConverter urlConverter = Mockito.mock(URLConverterImpl.class);
    Mockito.when(urlConverter.convertLink(exampleString)).thenThrow(new URLInvalidException("foobar"));
    ChannelManager tempChannelManager = new ChannelManager(
        channelName,
        new PermissionsManager(),
        new MessageManager(),
        new TimeoutManager(),
        new BlacklistManager(),
        new ChannelSettingDAOHashMapImpl(),
        urlConverter);
    expectedException.expect(ChannelOperationException.class);
    tempChannelManager.customiseURL(exampleString);
  }

  @Test
  public void getChannelSettingTest() throws Exception {
    String channelSetting = channelManager.getChannelSetting(ChannelSettingString.DEFAULT_PERMISSION);
    Assert.assertNotNull(channelSetting);
    Assert.assertEquals(ChannelSettingString.DEFAULT_PERMISSION.getDefault(), channelSetting);
  }
}