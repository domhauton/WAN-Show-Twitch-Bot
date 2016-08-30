package bot.view.cli.executors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.Collections;

import bot.channel.ChannelManager;
import bot.view.cli.BotCommandException;
import twitch.chat.data.OutboundTwitchMessage;

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
