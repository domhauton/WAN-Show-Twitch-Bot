package bot.commands;

import twitch.channel.data.TwitchMessage;
import twitch.chat.data.OutboundTwitchMessage;

import java.util.Collection;

/**
 * Created by Dominic Hauton on 06/05/2016.
 *
 * Interface for bot commands
 */
public interface BotCommand {
    /**
     * Parses a message
     */
    Collection<OutboundTwitchMessage> parseMessage(TwitchMessage twitchMessage);

    /**
     *
     * @return List of aliases for command
     */
    Collection<String> getCommandAlias();
}
