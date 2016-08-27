package bot.view.cli.executors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;

import bot.channel.ChannelManager;
import bot.view.cli.BotCommandException;
import twitch.chat.data.OutboundTwitchMessage;

/**
 * Created by Dominic Hauton on 11/07/2016.
 */
public class PermissionModifier implements CommandExecutor {

  @Override
  public Collection<OutboundTwitchMessage> executeCommand(ImmutableSet<Character> flags, ImmutableList<String> args, ChannelManager channelManager) throws BotCommandException {
    throw new BotCommandException("Command not yet implemented");
    //TODO Add ability to add and remove permissions at all levels
  }
}
