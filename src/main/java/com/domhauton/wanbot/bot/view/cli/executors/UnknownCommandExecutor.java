package com.domhauton.wanbot.bot.view.cli.executors;

import com.domhauton.wanbot.bot.channel.ChannelManager;
import com.domhauton.wanbot.bot.view.cli.BotCommandException;
import com.domhauton.wanbot.chat.data.OutboundTwitchMessage;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Created by Dominic Hauton on 11/07/2016.
 *
 * An implementation to an unknown command.
 */
public class UnknownCommandExecutor implements CommandExecutor {

  @Override
  public BotCommandResult executeCommand(ImmutableSet<Character> flags, ImmutableList<String> args, ChannelManager channelManager) throws BotCommandException {
    OutboundTwitchMessage outboundChannelResponse = new OutboundTwitchMessage(
        "Invalid Command Used",
        channelManager.getChannelName());
    //FIXME Return correct result
    return new BotCommandResult(null, null);
  }
}
