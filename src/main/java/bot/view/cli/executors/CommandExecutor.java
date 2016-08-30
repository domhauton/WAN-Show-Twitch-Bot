package bot.view.cli.executors;

import bot.view.cli.BotCommandException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import bot.channel.ChannelManager;
import twitch.chat.data.OutboundTwitchMessage;

import java.util.Collection;

/**
 * Created by Dominic Hauton on 29/05/2016.
 *
 *
 */
public interface CommandExecutor {
<<<<<<< f9ac3a962a7c90330d9d44adce2245674b350b67
    Collection<OutboundTwitchMessage> executeCommand(ImmutableSet<Character> flags,
                                                     ImmutableList<String> args,
                                                     ChannelManager channelManager) throws BotCommandException;
=======
  BotCommandResult executeCommand(ImmutableSet<Character> flags,
                                                   ImmutableList<String> args,
                                                   ChannelManager channelManager) throws BotCommandException;
>>>>>>> Connected Blacklist Executor
}
