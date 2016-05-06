package bot.commands;

import twitch.channel.data.TwitchMessage;
import twitch.chat.data.OutboundTwitchMessage;

import java.util.Collection;

/**
 * Created by Dominic Hauton on 06/05/2016.
 *
 * Adds a message to the message blacklist
 */
public class BlacklistWordCommand implements BotCommand {
    /**
     * Checks if the message needs to be blacklisted
     */
    @Override
    public Collection<OutboundTwitchMessage> parseMessage(TwitchMessage twitchMessage) {

    }
}
