package bot.view.cli.executors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import twitch.chat.data.OutboundTwitchMessage;

/**
 * Created by Dominic Hauton on 04/09/2016.
 *
 * Simple getter setter test
 */
public class BotCommandResultTest {
  private BotCommandResult botCommandResult;
  private Collection<OutboundTwitchMessage> twitchMessages;
  private String message = "this is a return message";

  @Before
  public void setUp() throws Exception {
    OutboundTwitchMessage twitchMessage1 = new OutboundTwitchMessage("foobar1", "target1");
    OutboundTwitchMessage twitchMessage2 = new OutboundTwitchMessage("foobar1", "target1");
    twitchMessages = Arrays.asList(twitchMessage1, twitchMessage2);
    botCommandResult = new BotCommandResult(twitchMessages, message);
  }

  @Test
  public void getOutboundTwitchMessages() throws Exception {
    Assert.assertEquals(twitchMessages, botCommandResult.getOutboundTwitchMessages());
  }

  @Test
  public void getCommandExecutionMessage() throws Exception {
    Assert.assertEquals(message, botCommandResult.getcommandExecutionMessage());
  }

}