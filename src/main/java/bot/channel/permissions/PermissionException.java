package bot.channel.permissions;

import bot.channel.ChannelOperationException;

/**
 * Created by Dominic Hauton on 10/08/2016.
 *
 * Thrown when issue in permission management occurs
 */
public class PermissionException extends ChannelOperationException {

    /**
     * Constructs a new exception with the specified detail MESSAGE.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail MESSAGE. The detail MESSAGE is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    PermissionException(String message) {
        super(message);
    }
}
