package twitch.channel.blacklist;

import twitch.channel.ChannelOperationException;

/**
 * Created by Dominic Hauton on 23/05/2016.
 *
 * Exception to throw when trying to add an existing blacklist entry.
 */
class BlacklistOperationOperationException extends ChannelOperationException {

    BlacklistOperationOperationException(String message) {
        super(message);
    }
}
