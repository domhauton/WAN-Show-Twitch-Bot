package twitch.channel.settings.exceptions;

import twitch.channel.ChannelOperationException;

/**
 * Created by Dominic Hauton on 26/06/2016.
 */
public class DatabaseException extends ChannelOperationException{

    public DatabaseException(final String message) {
        super(message);
    }
}
