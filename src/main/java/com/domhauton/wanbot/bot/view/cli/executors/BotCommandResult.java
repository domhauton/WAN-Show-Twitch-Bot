package com.domhauton.wanbot.bot.view.cli.executors;

import com.domhauton.wanbot.chat.data.OutboundTwitchMessage;

import java.util.Collection;

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
