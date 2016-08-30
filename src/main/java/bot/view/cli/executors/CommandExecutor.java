package bot.view.cli.executors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;

import bot.channel.ChannelManager;
import bot.view.cli.BotCommandException;
import twitch.chat.data.OutboundTwitchMessage;

/**
 * Created by Dominic Hauton on 29/05/2016.
 */
public interface CommandExecutor {
  BotCommandResult executeCommand(ImmutableSet<Character> flags,
                                                   ImmutableList<String> args,
                                                   ChannelManager channelManager) throws BotCommandException;
}
