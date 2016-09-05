package bot.view.cli.executors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import bot.channel.ChannelManager;
import bot.channel.TwitchUser;
import bot.channel.blacklist.BlacklistType;
import bot.channel.message.TwitchMessage;
import bot.view.cli.BotCommandException;

/**
 * Created by Dominic Hauton on 30/08/2016.
 *
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
  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  private ChannelManager channelManager;
  private BlacklistModifier blacklistModifier;

  @Before
  public void setUp() throws Exception {
    blacklistModifier = new BlacklistModifier();
    channelManager = new ChannelManager(CHANNEL_NAME);
  }

  @Test
  public void executeCommandSimpleDefaultWordAdditionTest() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of();
    ImmutableList<String> args = ImmutableList.of(CHANNEL_MESSAGE_1);
    Assert.assertTrue("Not blacklisted yet", channelManager.addChannelMessage(TWITCH_MESSAGE_1));
    blacklistModifier.executeCommand(flags, args, channelManager);
    Assert.assertFalse("Now blacklisted", channelManager.addChannelMessage(TWITCH_MESSAGE_1));
    Assert.assertFalse("This should also be blocked as word was blocked. Not just message",
        channelManager.addChannelMessage(TWITCH_MESSAGE_1_EXTENDED));
  }

  @Test
  public void executeCommandSimpleWordAdditionTest() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of('w');
    ImmutableList<String> args = ImmutableList.of(CHANNEL_MESSAGE_1);
    Assert.assertTrue("Not blacklisted yet", channelManager.addChannelMessage(TWITCH_MESSAGE_1));
    blacklistModifier.executeCommand(flags, args, channelManager);
    Assert.assertFalse("Now blacklisted", channelManager.addChannelMessage(TWITCH_MESSAGE_1));
    Assert.assertFalse("This should also be blocked as word was blocked. Not just message",
        channelManager.addChannelMessage(TWITCH_MESSAGE_1_EXTENDED));
  }

  @Test
  public void executeCommandSimpleWordDoubleAdditionTest() throws Exception {
    executeCommandSimpleWordAdditionTest();
    ImmutableSet<Character> flags = ImmutableSet.of('w');
    ImmutableList<String> args = ImmutableList.of(CHANNEL_MESSAGE_1);
    expectedException.expect(BotCommandException.class);
    blacklistModifier.executeCommand(flags, args, channelManager);
  }

  @Test
  public void executeCommandNonRetroWordDoubleAdditionTest() throws Exception {
    executeCommandSimpleWordAdditionTest();
    ImmutableSet<Character> flags = ImmutableSet.of('w', 'i');
    ImmutableList<String> args = ImmutableList.of(CHANNEL_MESSAGE_1);
    expectedException.expect(BotCommandException.class);
    blacklistModifier.executeCommand(flags, args, channelManager);
  }


  @Test
  public void executeCommandSimpleMessageAdditionTest() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of('m');
    ImmutableList<String> args = ImmutableList.of(CHANNEL_MESSAGE_1);
    Assert.assertTrue("Not blacklisted yet", channelManager.addChannelMessage(TWITCH_MESSAGE_1));
    blacklistModifier.executeCommand(flags, args, channelManager);
    Assert.assertFalse("Now blacklisted", channelManager.addChannelMessage(TWITCH_MESSAGE_1));
    Assert.assertTrue("This should also be blocked as word was blocked. Not just message",
        channelManager.addChannelMessage(TWITCH_MESSAGE_1_EXTENDED));
  }

  @Test
  public void executeCommandSimpleRegexAdditionTest() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of('r');
    ImmutableList<String> args = ImmutableList.of(".*" + CHANNEL_MESSAGE_1);
    Assert.assertTrue("Not blacklisted yet", channelManager.addChannelMessage(TWITCH_MESSAGE_1));
    blacklistModifier.executeCommand(flags, args, channelManager);
    Assert.assertFalse("Now blacklisted", channelManager.addChannelMessage(TWITCH_MESSAGE_1));
    Assert.assertTrue("This should not be blocked as space is at end.",
        channelManager.addChannelMessage(TWITCH_MESSAGE_1_EXTENDED));
    Assert.assertFalse("This should not be blocked as space is at start.",
        channelManager.addChannelMessage(TWITCH_MESSAGE_1_PREPENDED));
  }

  @Test
  public void executeCommandRemoveRegexTest() throws Exception {
    executeCommandSimpleRegexAdditionTest();
    ImmutableSet<Character> flags = ImmutableSet.of('d', 'r');
    ImmutableList<String> args = ImmutableList.of(".*" + CHANNEL_MESSAGE_1);
    blacklistModifier.executeCommand(flags, args, channelManager);
    Assert.assertTrue("Rule should now be removed",
        channelManager.addChannelMessage(TWITCH_MESSAGE_1_PREPENDED));
  }

  @Test
  public void executeCommandRemoveRegexFailTest() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of('d', 'r');
    ImmutableList<String> args = ImmutableList.of(".*" + CHANNEL_MESSAGE_1);
    expectedException.expect(BotCommandException.class);
    blacklistModifier.executeCommand(flags, args, channelManager);
  }


  @Test
  public void executeCommandMessageAdditionNoRetroactiveRemovalTest() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of('m', 'i');
    ImmutableList<String> args = ImmutableList.of(CHANNEL_MESSAGE_1);
    Assert.assertTrue("Not blacklisted yet", channelManager.addChannelMessage(TWITCH_MESSAGE_1));
    BotCommandResult botCommandResult = blacklistModifier.executeCommand(flags, args, channelManager);
    Assert.assertEquals("Should be 0 timeout messages",
        0,
        botCommandResult.getOutboundTwitchMessages()
            .stream()
            .filter(twitchMessage -> twitchMessage.getPayload().contains(".timeout"))
            .count());
  }

  @Test
  public void executeCommandMessageAdditionRetroactiveRemovalTest() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of('m');
    ImmutableList<String> args = ImmutableList.of(CHANNEL_MESSAGE_1);
    Assert.assertTrue("Not blacklisted yet", channelManager.addChannelMessage(TWITCH_MESSAGE_1));
    Assert.assertTrue("Not blacklisted yet", channelManager.addChannelMessage(TWITCH_MESSAGE_1));
    BotCommandResult botCommandResult
        = blacklistModifier.executeCommand(flags, args, channelManager);
    Assert.assertEquals("Should be 1 timeout message",
        2,
        botCommandResult.getOutboundTwitchMessages()
            .stream()
            .filter(twitchMessage -> twitchMessage.getPayload().contains(".timeout"))
            .count());
  }

  @Test
  public void executeCommandMessageRemovalFuzzyTest() throws Exception {
    executeCommandSimpleRegexAdditionTest();
    ImmutableSet<Character> flags = ImmutableSet.of('d', 'r', 'f');
    ImmutableList<String> args = ImmutableList.of(CHANNEL_MESSAGE_1.substring(4));
    blacklistModifier.executeCommand(flags, args, channelManager);
    Assert.assertTrue("Rule should now be removed",
        channelManager.addChannelMessage(TWITCH_MESSAGE_1_PREPENDED));
  }

  @Test
  public void executeCommandMessageRemovalFuzzyTestFail() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of('d', 'f');
    ImmutableList<String> args = ImmutableList.of(CHANNEL_MESSAGE_1.substring(4));
    expectedException.expect(BotCommandException.class);
    blacklistModifier.executeCommand(flags, args, channelManager);
  }

  @Test
  public void executeCommandEmptyArgsFail() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of('d', 'r', 'f');
    ImmutableList<String> args = ImmutableList.of();
    expectedException.expect(BotCommandException.class);
    blacklistModifier.executeCommand(flags, args, channelManager);
  }

  @Test
  public void executeCommandTwoArgsFail() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of('d', 'r', 'f');
    ImmutableList<String> args = ImmutableList.of("arg1", "arg2");
    expectedException.expect(BotCommandException.class);
    blacklistModifier.executeCommand(flags, args, channelManager);
  }

  @Test
  public void executeCommandMultiFlagAddition1Test() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of('w', 'm', 'r');
    ImmutableList<String> args = ImmutableList.of(CHANNEL_MESSAGE_1);
    Assert.assertTrue("Not blacklisted yet", channelManager.addChannelMessage(TWITCH_MESSAGE_1));
    blacklistModifier.executeCommand(flags, args, channelManager);
    Assert.assertFalse("Now blacklisted", channelManager.addChannelMessage(TWITCH_MESSAGE_1));
    Assert.assertFalse("This should also be blocked as word was blocked. Not just message",
        channelManager.addChannelMessage(TWITCH_MESSAGE_1_EXTENDED));
  }

  @Test
  public void executeCommandMultiFlagAddition2Test() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of('m', 'w', 'r');
    ImmutableList<String> args = ImmutableList.of(CHANNEL_MESSAGE_1);
    Assert.assertTrue("Not blacklisted yet", channelManager.addChannelMessage(TWITCH_MESSAGE_1));
    blacklistModifier.executeCommand(flags, args, channelManager);
    Assert.assertFalse("Now blacklisted", channelManager.addChannelMessage(TWITCH_MESSAGE_1));
    Assert.assertTrue("This should also be blocked as word was blocked. Not just message",
        channelManager.addChannelMessage(TWITCH_MESSAGE_1_EXTENDED));
  }

  @Test
  public void extractBlackListTypeSimpleWordTest() throws Exception {
    Collection<Character> chars = Collections.singletonList('w');
    BlacklistType actualBlacklistType = BlacklistModifier.extractBlackListType(chars);
    BlacklistType expectedBlacklistType = BlacklistType.WORD;
    Assert.assertEquals(expectedBlacklistType, actualBlacklistType);
  }

  @Test
  public void extractBlackListTypeSimpleMessageTest() throws Exception {
    Collection<Character> chars = Collections.singletonList('m');
    BlacklistType actualBlacklistType = BlacklistModifier.extractBlackListType(chars);
    BlacklistType expectedBlacklistType = BlacklistType.MESSAGE;
    Assert.assertEquals(expectedBlacklistType, actualBlacklistType);
  }

  @Test
  public void extractBlackListTypeSimpleRegexTest() throws Exception {
    Collection<Character> chars = Collections.singletonList('r');
    BlacklistType actualBlacklistType = BlacklistModifier.extractBlackListType(chars);
    BlacklistType expectedBlacklistType = BlacklistType.REGEX;
    Assert.assertEquals(expectedBlacklistType, actualBlacklistType);
  }

  @Test
  public void extractBlackListTypeSimpleEmptyTest() throws Exception {
    Collection<Character> chars = Collections.emptyList();
    BlacklistType actualBlacklistType = BlacklistModifier.extractBlackListType(chars);
    BlacklistType expectedBlacklistType = BlacklistType.WORD;
    Assert.assertEquals(expectedBlacklistType, actualBlacklistType);
  }

  @Test
  public void extractBlackListTypeSimpleDoubleTest() throws Exception {
    Collection<Character> chars = Arrays.asList('w', 'm');
    BlacklistType actualBlacklistType = BlacklistModifier.extractBlackListType(chars);
    BlacklistType expectedBlacklistType = BlacklistType.WORD;
    Assert.assertEquals(expectedBlacklistType, actualBlacklistType);
  }

  @Test
  public void extractBlackListTypeIrrelevantFlagTest() throws Exception {
    Collection<Character> chars = Arrays.asList('y', 'x');
    BlacklistType actualBlacklistType = BlacklistModifier.extractBlackListType(chars);
    BlacklistType expectedBlacklistType = BlacklistType.WORD;
    Assert.assertEquals(expectedBlacklistType, actualBlacklistType);
  }
}