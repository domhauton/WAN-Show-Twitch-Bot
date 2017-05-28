package com.domhauton.wanbot.bot.view.cli.executors;

import com.domhauton.wanbot.bot.channel.ChannelManager;
import com.domhauton.wanbot.bot.view.cli.BotCommandException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Created by Dominic Hauton on 29/05/2016.
 */
public interface CommandExecutor {
  BotCommandResult executeCommand(ImmutableSet<Character> flags,
                                                   ImmutableList<String> args,
                                                   ChannelManager channelManager) throws BotCommandException;
}
