package bot.view.cli.executors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import bot.channel.ChannelManager;
import bot.view.cli.BotCommandException;

/**
 * Created by Dominic Hauton on 11/07/2016.
 */
public class ChannelSettingModifier implements CommandExecutor {
  @Override
  public BotCommandResult executeCommand(ImmutableSet<Character> flags, ImmutableList<String> args, ChannelManager channelManager) throws BotCommandException {
    throw new BotCommandException("Command not yet implemented");
    //TODO Add ability to set the show start time and modify settings
  }
}
