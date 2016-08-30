package bot.view.cli.executors;

import java.util.Collection;

import twitch.chat.data.OutboundTwitchMessage;

/**
 * Created by Dominic Hauton on 27/08/2016.
 *
 * Data class used to bundle returns from command executions.
 */
public class BotCommandResult {
  private final Collection<OutboundTwitchMessage> outboundTwitchMessages;
  private final String commandExecutionMessage;

  BotCommandResult(Collection<OutboundTwitchMessage> outboundTwitchMessages, String commandExecutionMessage) {
    this.outboundTwitchMessages = outboundTwitchMessages;
    this.commandExecutionMessage = commandExecutionMessage;
  }

  public Collection<OutboundTwitchMessage> getOutboundTwitchMessages() {
    return outboundTwitchMessages;
  }

  public String getcommandExecutionMessage() {
    return commandExecutionMessage;
  }
}
