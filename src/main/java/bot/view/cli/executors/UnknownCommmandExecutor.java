package bot.view.cli.executors;

import bot.view.cli.BotCommandException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import bot.channel.ChannelManager;
import twitch.chat.data.OutboundTwitchMessage;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by Dominic Hauton on 11/07/2016.
 *
 * An implementation to an unknown command.
 */
public class UnknownCommmandExecutor implements CommandExecutor {

    @Override
    public Collection<OutboundTwitchMessage> executeCommand(ImmutableSet<Character> flags, ImmutableList<String> args, ChannelManager channelManager) throws BotCommandException {
        OutboundTwitchMessage outboundChannelResponse = new OutboundTwitchMessage(
                "Invalid Command Used",
                channelManager.getChannelName());
        return Collections.singletonList(outboundChannelResponse);
    }
}
