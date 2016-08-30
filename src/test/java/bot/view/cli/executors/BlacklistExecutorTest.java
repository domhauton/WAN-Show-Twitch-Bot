package bot.view.cli.executors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import bot.channel.ChannelManager;
import bot.channel.TwitchUser;
import bot.channel.blacklist.BlacklistType;
import bot.channel.message.TwitchMessage;

/**
 * Created by Dominic Hauton on 30/08/2016.
 *
 * Test all aspects of the Blacklist executor
 */
public class BlacklistExecutorTest {

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
  private BlacklistExecutor blacklistExecutor;


  @Before
  public void setUp() throws Exception {
    blacklistExecutor = new BlacklistExecutor();
    channelManager = new ChannelManager(CHANNEL_NAME);
  }

  @Test
  public void executeCommandSimpleDefaultWordAdditionTest() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of();
    ImmutableList<String> args = ImmutableList.of(CHANNEL_MESSAGE_1);
    Assert.assertTrue("Not blacklisted yet", channelManager.addChannelMessage(TWITCH_MESSAGE_1));
    blacklistExecutor.executeCommand(flags, args, channelManager);
    Assert.assertFalse("Now blacklisted", channelManager.addChannelMessage(TWITCH_MESSAGE_1));
    Assert.assertFalse("This should also be blocked as word was blocked. Not just message",
        channelManager.addChannelMessage(TWITCH_MESSAGE_1_EXTENDED));
  }

  @Test
  public void executeCommandSimpleWordAdditionTest() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of('w');
    ImmutableList<String> args = ImmutableList.of(CHANNEL_MESSAGE_1);
    Assert.assertTrue("Not blacklisted yet", channelManager.addChannelMessage(TWITCH_MESSAGE_1));
    blacklistExecutor.executeCommand(flags, args, channelManager);
    Assert.assertFalse("Now blacklisted", channelManager.addChannelMessage(TWITCH_MESSAGE_1));
    Assert.assertFalse("This should also be blocked as word was blocked. Not just message",
        channelManager.addChannelMessage(TWITCH_MESSAGE_1_EXTENDED));
  }

  @Test
  public void executeCommandSimpleMessageAdditionTest() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of('m');
    ImmutableList<String> args = ImmutableList.of(CHANNEL_MESSAGE_1);
    Assert.assertTrue("Not blacklisted yet", channelManager.addChannelMessage(TWITCH_MESSAGE_1));
    blacklistExecutor.executeCommand(flags, args, channelManager);
    Assert.assertFalse("Now blacklisted", channelManager.addChannelMessage(TWITCH_MESSAGE_1));
    Assert.assertTrue("This should also be blocked as word was blocked. Not just message",
        channelManager.addChannelMessage(TWITCH_MESSAGE_1_EXTENDED));
  }

  @Test
  public void executeCommandSimpleRegexAdditionTest() throws Exception {
    ImmutableSet<Character> flags = ImmutableSet.of('r');
    ImmutableList<String> args = ImmutableList.of(".*" + CHANNEL_MESSAGE_1);
    Assert.assertTrue("Not blacklisted yet", channelManager.addChannelMessage(TWITCH_MESSAGE_1));
    blacklistExecutor.executeCommand(flags, args, channelManager);
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
    blacklistExecutor.executeCommand(flags, args, channelManager);
    Assert.assertTrue("Rule should now be removed",
        channelManager.addChannelMessage(TWITCH_MESSAGE_1_PREPENDED));
  }

  //TODO Test for retroactive message removal.
  //TODO Test for non-retroactive ban.
  //TODO Test for fuzzy removal exact.
  //TODO Test for fuzzy removal fuzzy.
  //TODO Test for addition fail due to already existing.
  //TODO Test for non-retroactive addition fail.
  //TODO Test for basic removal fail
  //TODO Test for fuzzy removal fail
  //TODO Test for empty/incorrect number of args fail.
  //TODO Test for empty or null flags fail.
  //TODO Test for multi-flags addition

  @Test
  public void extractBlackListTypeSimpleWordTest() throws Exception {
    Collection<Character> chars = Collections.singletonList('w');
    BlacklistType actualBlacklistType = BlacklistExecutor.extractBlackListType(chars);
    BlacklistType expectedBlacklistType = BlacklistType.WORD;
    Assert.assertEquals(expectedBlacklistType, actualBlacklistType);
  }

  @Test
  public void extractBlackListTypeSimpleMessageTest() throws Exception {
    Collection<Character> chars = Collections.singletonList('m');
    BlacklistType actualBlacklistType = BlacklistExecutor.extractBlackListType(chars);
    BlacklistType expectedBlacklistType = BlacklistType.MESSAGE;
    Assert.assertEquals(expectedBlacklistType, actualBlacklistType);
  }

  @Test
  public void extractBlackListTypeSimpleRegexTest() throws Exception {
    Collection<Character> chars = Collections.singletonList('r');
    BlacklistType actualBlacklistType = BlacklistExecutor.extractBlackListType(chars);
    BlacklistType expectedBlacklistType = BlacklistType.REGEX;
    Assert.assertEquals(expectedBlacklistType, actualBlacklistType);
  }

  @Test
  public void extractBlackListTypeSimpleEmptyTest() throws Exception {
    Collection<Character> chars = Collections.emptyList();
    BlacklistType actualBlacklistType = BlacklistExecutor.extractBlackListType(chars);
    BlacklistType expectedBlacklistType = BlacklistType.WORD;
    Assert.assertEquals(expectedBlacklistType, actualBlacklistType);
  }

  @Test
  public void extractBlackListTypeSimpleDoubleTest() throws Exception {
    Collection<Character> chars = Arrays.asList('w', 'm');
    BlacklistType actualBlacklistType = BlacklistExecutor.extractBlackListType(chars);
    BlacklistType expectedBlacklistType = BlacklistType.WORD;
    Assert.assertEquals(expectedBlacklistType, actualBlacklistType);
  }

  @Test
  public void extractBlackListTypeIrrelevantFlagTest() throws Exception {
    Collection<Character> chars = Arrays.asList('y', 'x');
    BlacklistType actualBlacklistType = BlacklistExecutor.extractBlackListType(chars);
    BlacklistType expectedBlacklistType = BlacklistType.WORD;
    Assert.assertEquals(expectedBlacklistType, actualBlacklistType);
  }
}