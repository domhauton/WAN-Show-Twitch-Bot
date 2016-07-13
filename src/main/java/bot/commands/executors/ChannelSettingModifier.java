package bot.commands.executors;

import bot.commands.BotCommandException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import twitch.channel.ChannelManager;
import twitch.chat.data.OutboundTwitchMessage;

import java.util.Collection;

/**
 * Created by Dominic Hauton on 11/07/2016.
 */
public class ChannelSettingModifier implements CommandExecutor{
    @Override
    public Collection<OutboundTwitchMessage> executeCommand(ImmutableSet<Character> flags, ImmutableList<String> args, ChannelManager channelManager) throws BotCommandException {
        throw new BotCommandException("Command not yet implemented");
        //TODO Add ability to set the show start time and modify settings
    }
}
