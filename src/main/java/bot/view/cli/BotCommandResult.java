package bot.view.cli;

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
  private final boolean isSuccessful;

  public BotCommandResult(Collection<OutboundTwitchMessage> outboundTwitchMessages, String commandExecutionMessage, boolean isSuccessful) {
    this.outboundTwitchMessages = outboundTwitchMessages;
    this.commandExecutionMessage = commandExecutionMessage;
    this.isSuccessful = isSuccessful;
  }

  public Collection<OutboundTwitchMessage> getOutboundTwitchMessages() {
    return outboundTwitchMessages;
  }

  public String getcommandExecutionMessage() {
    return commandExecutionMessage;
  }

  public boolean isSuccessful() {
    return isSuccessful;
  }
}
