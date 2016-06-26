package bot.parsers;

import twitch.channel.message.TwitchMessage;
import twitch.channel.permissions.UserPermission;
import twitch.chat.data.OutboundTwitchMessage;

import java.util.Collection;

/**
 * Created by Dominic Hauton on 03/05/2016.
 *
 * Provides methods required for parsing message
 */
public interface TwitchMessageParser {
    /**
     * Returns a collection of responses to the given InboundMessage
     * @return Responses for message.
     */
    Collection<OutboundTwitchMessage> parseMessage(TwitchMessage inboundTwitchMessage);

    /**
     * @return true if message should be sent through parser.
     */
    boolean isParsingRequired(UserPermission userPermission);
}
