package bot.view.cli.executors;

import bot.channel.ChannelManager;
import bot.channel.TwitchUser;
import bot.channel.blacklist.BlacklistType;
import bot.channel.message.TwitchMessage;
import bot.view.cli.BotCommandException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by Dominic Hauton on 30/08/2016.
 * <p>
 * Test all aspects of the Blacklist executor
 */
public class BlacklistModifierTest {

  private static final String CHANNEL_NAME = "foobarChannel";
  private static final String CHANNEL_MESSAGE_1 = "foobar";
  private static final TwitchUser CHANNEL_USER_1 = new TwitchUser("userFoo1");
  private static final TwitchMessage TWITCH_MESSAGE_1 =
      new TwitchMessage(CHANNEL_MESSAGE_1, CHANNEL_USER_1, DateTime.now(), CHANNEL_NAME);
  private static final TwitchMessage TWITCH_MESSAGE_1_EXTENDED =
      new TwitchMessage(CHANNEL_MESSAGE_1 + " ", CHANNEL_USER_1, DateTime.now(), CHANNEL_NAME);
  private static final TwitchMessage TWITCH_MESSAGE_1_PREPENDED =
      new TwitchMessage(" " + CHANNEL_MESSAGE_1, CHANNEL_USER_1, DateTime.now(), CHANNEL_NAME);


  private ChannelManager channelManager;
  private BlacklistModifier blacklistModifier;

  @BeforeEach
  void setUp() throws Exception {
    blacklistModifier = new BlacklistModifier();
    channelManager = new ChannelManager(CHANNEL_NAME);
  }

  @Test
  void executeCommandSimpleDefaultWordAdditionTest() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of();
    ImmutableList<String> args = ImmutableList.of(CHANNEL_MESSAGE_1);
    Assertions.assertTrue(channelManager.addChannelMessage(TWITCH_MESSAGE_1), "Not blacklisted yet");
    blacklistModifier.executeCommand(flags, args, channelManager);
    Assertions.assertFalse(channelManager.addChannelMessage(TWITCH_MESSAGE_1), "Now blacklisted");
    Assertions.assertFalse(channelManager.addChannelMessage(TWITCH_MESSAGE_1_EXTENDED),
        "This should also be blocked as word was blocked. Not just message");
  }

  @Test
  void executeCommandSimpleWordAdditionTest() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of('w');
    ImmutableList<String> args = ImmutableList.of(CHANNEL_MESSAGE_1);
    Assertions.assertTrue(channelManager.addChannelMessage(TWITCH_MESSAGE_1),
        "Not blacklisted yet");
    blacklistModifier.executeCommand(flags, args, channelManager);
    Assertions.assertFalse(channelManager.addChannelMessage(TWITCH_MESSAGE_1), "Now blacklisted");
    Assertions.assertFalse(channelManager.addChannelMessage(TWITCH_MESSAGE_1_EXTENDED),
        "This should also be blocked as word was blocked. Not just message");
  }

  @Test
  void executeCommandSimpleWordDoubleAdditionTest() throws Exception {
    executeCommandSimpleWordAdditionTest();
    ImmutableSet<Character> flags = ImmutableSet.of('w');
    ImmutableList<String> args = ImmutableList.of(CHANNEL_MESSAGE_1);
    Assertions.assertThrows(BotCommandException.class,
        () -> blacklistModifier.executeCommand(flags, args, channelManager));
  }

  @Test
  void executeCommandNonRetroWordDoubleAdditionTest() throws Exception {
    executeCommandSimpleWordAdditionTest();
    ImmutableSet<Character> flags = ImmutableSet.of('w', 'i');
    ImmutableList<String> args = ImmutableList.of(CHANNEL_MESSAGE_1);
    Assertions.assertThrows(BotCommandException.class,
        () -> blacklistModifier.executeCommand(flags, args, channelManager));
  }


  @Test
  void executeCommandSimpleMessageAdditionTest() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of('m');
    ImmutableList<String> args = ImmutableList.of(CHANNEL_MESSAGE_1);
    Assertions.assertTrue(channelManager.addChannelMessage(TWITCH_MESSAGE_1), "Not blacklisted yet");
    blacklistModifier.executeCommand(flags, args, channelManager);
    Assertions.assertFalse(channelManager.addChannelMessage(TWITCH_MESSAGE_1), "Now blacklisted");
    Assertions.assertTrue(channelManager.addChannelMessage(TWITCH_MESSAGE_1_EXTENDED),
        "This should also be blocked as word was blocked. Not just message");
  }

  @Test
  void executeCommandSimpleRegexAdditionTest() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of('r');
    ImmutableList<String> args = ImmutableList.of(".*" + CHANNEL_MESSAGE_1);
    Assertions.assertTrue(channelManager.addChannelMessage(TWITCH_MESSAGE_1),
        "Not blacklisted yet");
    blacklistModifier.executeCommand(flags, args, channelManager);
    Assertions.assertFalse(channelManager.addChannelMessage(TWITCH_MESSAGE_1),
        "Now blacklisted");
    Assertions.assertTrue(channelManager.addChannelMessage(TWITCH_MESSAGE_1_EXTENDED),
        "This should not be blocked as space is at end.");
    Assertions.assertFalse(channelManager.addChannelMessage(TWITCH_MESSAGE_1_PREPENDED),
        "This should not be blocked as space is at start.");
  }

  @Test
  void executeCommandRemoveRegexTest() throws Exception {
    executeCommandSimpleRegexAdditionTest();
    ImmutableSet<Character> flags = ImmutableSet.of('d', 'r');
    ImmutableList<String> args = ImmutableList.of(".*" + CHANNEL_MESSAGE_1);
    blacklistModifier.executeCommand(flags, args, channelManager);
    Assertions.assertTrue(channelManager.addChannelMessage(TWITCH_MESSAGE_1_PREPENDED),
        "Rule should now be removed");
  }

  @Test
  void executeCommandRemoveRegexFailTest() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of('d', 'r');
    ImmutableList<String> args = ImmutableList.of(".*" + CHANNEL_MESSAGE_1);
    Assertions.assertThrows(BotCommandException.class,
        () -> blacklistModifier.executeCommand(flags, args, channelManager));
  }


  @Test
  void executeCommandMessageAdditionNoRetroactiveRemovalTest() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of('m', 'i');
    ImmutableList<String> args = ImmutableList.of(CHANNEL_MESSAGE_1);
    Assertions.assertTrue(channelManager.addChannelMessage(TWITCH_MESSAGE_1), "Not blacklisted yet");
    BotCommandResult botCommandResult = blacklistModifier.executeCommand(flags, args, channelManager);
    Assertions.assertEquals(0,
        botCommandResult.getOutboundTwitchMessages()
            .stream()
            .filter(twitchMessage -> twitchMessage.getPayload().contains(".timeout"))
            .count(),
        "Should be 0 timeout messages");
  }

  @Test
  void executeCommandMessageAdditionRetroactiveRemovalTest() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of('m');
    ImmutableList<String> args = ImmutableList.of(CHANNEL_MESSAGE_1);
    Assertions.assertTrue(channelManager.addChannelMessage(TWITCH_MESSAGE_1), "Not blacklisted yet");
    Assertions.assertTrue(channelManager.addChannelMessage(TWITCH_MESSAGE_1), "Not blacklisted yet");
    BotCommandResult botCommandResult
        = blacklistModifier.executeCommand(flags, args, channelManager);
    Assertions.assertEquals(2,
        botCommandResult.getOutboundTwitchMessages()
            .stream()
            .filter(twitchMessage -> twitchMessage.getPayload().contains(".timeout"))
            .count(),
        "Should be 1 timeout message");
  }

  @Test
  void executeCommandMessageRemovalFuzzyTest() throws Exception {
    executeCommandSimpleRegexAdditionTest();
    ImmutableSet<Character> flags = ImmutableSet.of('d', 'r', 'f');
    ImmutableList<String> args = ImmutableList.of(CHANNEL_MESSAGE_1.substring(4));
    blacklistModifier.executeCommand(flags, args, channelManager);
    Assertions.assertTrue(channelManager.addChannelMessage(TWITCH_MESSAGE_1_PREPENDED),
        "Rule should now be removed");
  }

  @Test
  void executeCommandMessageRemovalFuzzyTestFail() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of('d', 'f');
    ImmutableList<String> args = ImmutableList.of(CHANNEL_MESSAGE_1.substring(4));
    Assertions.assertThrows(BotCommandException.class,
        () -> blacklistModifier.executeCommand(flags, args, channelManager));
  }

  @Test
  void executeCommandEmptyArgsFail() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of('d', 'r', 'f');
    ImmutableList<String> args = ImmutableList.of();
    Assertions.assertThrows(BotCommandException.class,
        () -> blacklistModifier.executeCommand(flags, args, channelManager));
  }

  @Test
  void executeCommandTwoArgsFail() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of('d', 'r', 'f');
    ImmutableList<String> args = ImmutableList.of("arg1", "arg2");
    Assertions.assertThrows(BotCommandException.class,
        () -> blacklistModifier.executeCommand(flags, args, channelManager));
  }

  @Test
  void executeCommandMultiFlagAddition1Test() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of('w', 'm', 'r');
    ImmutableList<String> args = ImmutableList.of(CHANNEL_MESSAGE_1);
    Assertions.assertTrue(channelManager.addChannelMessage(TWITCH_MESSAGE_1), "Not blacklisted yet");
    blacklistModifier.executeCommand(flags, args, channelManager);
    Assertions.assertFalse(channelManager.addChannelMessage(TWITCH_MESSAGE_1), "Now blacklisted");
    Assertions.assertFalse(channelManager.addChannelMessage(TWITCH_MESSAGE_1_EXTENDED),
        "This should also be blocked as word was blocked. Not just message");
  }

  @Test
  void executeCommandMultiFlagAddition2Test() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of('m', 'w', 'r');
    ImmutableList<String> args = ImmutableList.of(CHANNEL_MESSAGE_1);
    Assertions.assertTrue(channelManager.addChannelMessage(TWITCH_MESSAGE_1), "Not blacklisted yet");
    blacklistModifier.executeCommand(flags, args, channelManager);
    Assertions.assertFalse(channelManager.addChannelMessage(TWITCH_MESSAGE_1), "Now blacklisted");
    Assertions.assertTrue(channelManager.addChannelMessage(TWITCH_MESSAGE_1_EXTENDED),
        "This should also be blocked as word was blocked. Not just message");
  }

  @Test
  void extractBlackListTypeSimpleWordTest() throws Exception {
    Collection<Character> chars = Collections.singletonList('w');
    BlacklistType actualBlacklistType = BlacklistModifier.extractBlackListType(chars);
    BlacklistType expectedBlacklistType = BlacklistType.WORD;
    Assertions.assertEquals(expectedBlacklistType, actualBlacklistType);
  }

  @Test
  void extractBlackListTypeSimpleMessageTest() throws Exception {
    Collection<Character> chars = Collections.singletonList('m');
    BlacklistType actualBlacklistType = BlacklistModifier.extractBlackListType(chars);
    BlacklistType expectedBlacklistType = BlacklistType.MESSAGE;
    Assertions.assertEquals(expectedBlacklistType, actualBlacklistType);
  }

  @Test
  void extractBlackListTypeSimpleRegexTest() throws Exception {
    Collection<Character> chars = Collections.singletonList('r');
    BlacklistType actualBlacklistType = BlacklistModifier.extractBlackListType(chars);
    BlacklistType expectedBlacklistType = BlacklistType.REGEX;
    Assertions.assertEquals(expectedBlacklistType, actualBlacklistType);
  }

  @Test
  void extractBlackListTypeSimpleEmptyTest() throws Exception {
    Collection<Character> chars = Collections.emptyList();
    BlacklistType actualBlacklistType = BlacklistModifier.extractBlackListType(chars);
    BlacklistType expectedBlacklistType = BlacklistType.WORD;
    Assertions.assertEquals(expectedBlacklistType, actualBlacklistType);
  }

  @Test
  void extractBlackListTypeSimpleDoubleTest() throws Exception {
    Collection<Character> chars = Arrays.asList('w', 'm');
    BlacklistType actualBlacklistType = BlacklistModifier.extractBlackListType(chars);
    BlacklistType expectedBlacklistType = BlacklistType.WORD;
    Assertions.assertEquals(expectedBlacklistType, actualBlacklistType);
  }

  @Test
  void extractBlackListTypeIrrelevantFlagTest() throws Exception {
    Collection<Character> chars = Arrays.asList('y', 'x');
    BlacklistType actualBlacklistType = BlacklistModifier.extractBlackListType(chars);
    BlacklistType expectedBlacklistType = BlacklistType.WORD;
    Assertions.assertEquals(expectedBlacklistType, actualBlacklistType);
  }
}