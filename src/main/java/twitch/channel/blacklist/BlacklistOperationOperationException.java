package twitch.channel.blacklist;

import twitch.channel.ChannelOperationException;

/**
 * Created by Dominic Hauton on 23/05/2016.
 *
 * Exception to throw when trying to add an existing blacklist entry.
 */
public class BlacklistOperationOperationException extends ChannelOperationException {

    public BlacklistOperationOperationException(String message) {
        super(message);
    }
}
