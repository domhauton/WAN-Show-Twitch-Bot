package bot.channel;

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
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import url.URLConverter;
import url.URLConverterImpl;
import url.URLInvalidException;

import java.util.Collection;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by Dominic Hauton on 22/08/2016.
 * <p>
 * Main test for all channel manager methods.
 */
class ChannelManagerTest {

  private static String channelName = "fooChannel";
  private static String payload1 = "foobarMessage1";

  private ChannelManager channelManager;
  private TwitchUser twitchUser1;
  private TwitchMessage twitchMessage1;

  @BeforeEach
  void setUp() throws Exception {
    channelManager = new ChannelManager(channelName);
    twitchUser1 = new TwitchUser("foobarUser1");
    twitchMessage1 = new TwitchMessage(payload1, twitchUser1, DateTime.now(), channelName);
  }

  @Test
  void permissionSimpleTest() throws Exception {
    channelManager.setPermission(twitchUser1, UserPermission.BotAdmin);
    Assertions.assertFalse(channelManager.checkPermission(twitchUser1, UserPermission.ChannelOwner), "Should not have permissions for ChannelOwner.");
    Assertions.assertTrue(channelManager.checkPermission(twitchUser1, UserPermission.BotAdmin), "Should have permissions for BotAdmin.");
    Assertions.assertFalse(channelManager.checkPermission(twitchUser1, UserPermission.ChannelOwner), "Should have permissions for BotAdmin.");
    Assertions.assertFalse(channelManager.checkPermission(twitchUser1, UserPermission.ChannelOwner), "Should have permissions for ChannelOwner.");
  }

  @Test
  void permissionSetAndGetTest() throws Exception {
    Stream.of(UserPermission.values()).forEach(this::permissionsSetAndTestSingle);
  }

  private void permissionsSetAndTestSingle(UserPermission userPermission) {
    channelManager.setPermission(twitchUser1, userPermission);
    UserPermission actualPermission = null;
    try {
      actualPermission = channelManager.getPermission(twitchUser1);
    } catch (ChannelOperationException e) {
      Assertions.fail(e.getMessage());
    }
    Assertions.assertEquals(userPermission, actualPermission, "Value just set, should be identical");
  }

  @Test
  void permissionGetWithoutSetTest() throws Exception {
    UserPermission permission = channelManager.getPermission(twitchUser1);
    Assertions.assertNotNull(permission, "Should have fetched default");
  }

  @Test
  void permissionGetWithoutSetFail() throws Exception {
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

    Assertions.assertThrows(ChannelOperationException.class,
        () -> tempChannelManager.getPermission(twitchUser1));
  }

  @Test
  void messageManagerSimpleTest() throws Exception {
    channelManager.addChannelMessage(twitchMessage1);
    Assertions.assertEquals(1, channelManager.getMessageSnapshot().containsSimplePayload(payload1),
        "Should detect new message");
    Assertions.assertEquals(1, channelManager.getMessageSnapshot(twitchUser1).containsSimplePayload(payload1),
        "Should detect new message");
  }

  @Test
  void messageManagerOverflowTest() throws Exception {
    // Insert very high number of messages.
    IntStream.range(1, 1000).boxed().forEach(x -> addChannelMessageUnsafe(twitchMessage1));
    Assertions.assertEquals(100, channelManager.getMessageSnapshot().containsSimplePayload(payload1),
        "Messages should be deleted after reaching cap. If fail, check cap has not changed!");
    Assertions.assertEquals(10, channelManager.getMessageSnapshot(twitchUser1).containsSimplePayload(payload1),
        "Messages should be deleted after reaching cap. If fail, check cap has not changed!");
  }

  @Test
  void failMessageInsertTest() throws Exception {
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
    Assertions.assertThrows(ChannelOperationException.class,
        () -> tempChannelManager.addChannelMessage(twitchMessage));
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
  void userTimeoutSimpleTest() throws Exception {
    Duration actualTimeout1 = channelManager.addUserTimeout(twitchUser1.getUsername(), TimeoutReason
        .MESSAGE_RATE);
    Assertions.assertTrue(actualTimeout1.isLongerThan(Duration.ZERO), "actualTimeout should be above zero");
    Duration actualTimeout2 = channelManager.addUserTimeout(twitchUser1.getUsername(), TimeoutReason.MESSAGE_RATE);
    Assertions.assertTrue(actualTimeout2.isLongerThan(actualTimeout1), "actualTimeout should be above timeout1");
    Duration actualTimeoutRetrieved = channelManager.getUserTimeout(twitchUser1);
    Assertions.assertEquals(actualTimeout2, actualTimeoutRetrieved, "Returned and retrieved timeouts should be identical");
  }

  @Test
  void blackListItemSimpleTest() throws Exception {
    Collection<TwitchMessage> retroBannedMessages = channelManager.blacklistItem("foobar", BlacklistType.WORD);
    Assertions.assertTrue(retroBannedMessages.isEmpty(), "There should be no messages in the ban list yet!");
    Assertions.assertFalse(channelManager.addChannelMessage(twitchMessage1), "Message should be blacklisted!");
    Collection<TwitchMessage> retroBannedMessages2 = channelManager.blacklistItem(payload1, BlacklistType.MESSAGE);
    Assertions.assertFalse(retroBannedMessages2.isEmpty(), "The added message should be in the list.");
  }

  @Test
  void blackListItemRetroactiveTest() throws Exception {
    String regexToBlacklist = ".*foobar.*";
    int messagesToAdd = 10;
    IntStream.range(0, messagesToAdd).boxed().forEach(x -> addChannelMessageUnsafe(twitchMessage1));
    Collection<TwitchMessage> retroBannedMessages = channelManager.blacklistItem(regexToBlacklist, BlacklistType
        .REGEX);
    Assertions.assertEquals(messagesToAdd, retroBannedMessages.size(), "All added messages should break rule");
    channelManager.removeBlacklistItem(regexToBlacklist, BlacklistType.REGEX);
    Collection<TwitchMessage> retroBannedMessagesNone = channelManager.blacklistItem(regexToBlacklist, BlacklistType
        .REGEX, 0);
    Assertions.assertTrue(retroBannedMessagesNone.isEmpty(), "Ensure no retroactive bans.");
    channelManager.removeBlacklistItem(regexToBlacklist, BlacklistType.REGEX);
    Collection<TwitchMessage> retroBannedMessagesNeg = channelManager.blacklistItem(regexToBlacklist, BlacklistType
        .REGEX, -1);
    Assertions.assertTrue(retroBannedMessagesNeg.isEmpty(), "Ensure no retroactive bans.");
  }

  @Test
  void blackListRemoveItemTest() throws Exception {
    String finalTestMessage = "A final test for item 3";
    channelManager.blacklistItem("Test Item 1", BlacklistType.MESSAGE);
    channelManager.blacklistItem("A test for Item 2", BlacklistType.WORD);
    channelManager.blacklistItem(finalTestMessage, BlacklistType.REGEX);
    Collection<BlacklistEntry> blacklistEntries = channelManager.removeBlacklistItem("Item");
    Assertions.assertEquals(2, blacklistEntries.size(), "Should have removed two entries");
    TwitchMessage twitchMessage = new TwitchMessage(finalTestMessage, twitchUser1, DateTime.now(), channelName);
    Assertions.assertFalse(channelManager.addChannelMessage(twitchMessage), "Should not have removed mis-capitalised entry");
  }

  @Test
  void blackListItemRemoveFail() throws Exception {
    Assertions.assertThrows(ChannelOperationException.class,
        () -> channelManager.removeBlacklistItem("foobar", BlacklistType.REGEX));
  }

  @Test
  void blackListItemAddTwiceTest() throws Exception {
    channelManager.blacklistItem("foobar1", BlacklistType.REGEX);
    Assertions.assertThrows(ChannelOperationException.class,
        () -> channelManager.blacklistItem("foobar1", BlacklistType.REGEX));

  }

  @Test
  void getChannelNameTest() throws Exception {
    String actualChannelName = channelManager.getChannelName();
    Assertions.assertEquals(actualChannelName, channelName);
  }

  @Test
  void customiseURLTest() throws Exception {
    String exampleString = "foobar";
    String actualString = channelManager.customiseURL(exampleString);
    Assertions.assertEquals(exampleString, actualString);
  }

  @Test
  void customiseURLFailTest() throws Exception {
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
    Assertions.assertThrows(ChannelOperationException.class,
        () -> tempChannelManager.customiseURL(exampleString));
  }

  @Test
  void getChannelSettingTest() throws Exception {
    String channelSetting = channelManager.getChannelSetting(ChannelSettingString.DEFAULT_PERMISSION);
    Assertions.assertNotNull(channelSetting);
    Assertions.assertEquals(ChannelSettingString.DEFAULT_PERMISSION.getDefault(), channelSetting);
  }
}