package bot.view.cli.executors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import twitch.chat.data.OutboundTwitchMessage;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Dominic Hauton on 04/09/2016.
 * <p>
 * Simple getter setter test
 */
class BotCommandResultTest {
  private BotCommandResult botCommandResult;
  private Collection<OutboundTwitchMessage> twitchMessages;
  private String message = "this is a return message";

  @BeforeEach
  void setUp() throws Exception {
    OutboundTwitchMessage twitchMessage1 = new OutboundTwitchMessage("foobar1", "target1");
    OutboundTwitchMessage twitchMessage2 = new OutboundTwitchMessage("foobar1", "target1");
    twitchMessages = Arrays.asList(twitchMessage1, twitchMessage2);
    botCommandResult = new BotCommandResult(twitchMessages, message);
  }

  @Test
  void getOutboundTwitchMessages() throws Exception {
    Assertions.assertEquals(twitchMessages, botCommandResult.getOutboundTwitchMessages());
  }

  @Test
  void getCommandExecutionMessage() throws Exception {
    Assertions.assertEquals(message, botCommandResult.getcommandExecutionMessage());
  }

}