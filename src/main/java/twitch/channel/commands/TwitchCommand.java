package twitch.channel.commands;

import twitch.channel.data.TwitchMessage;
import twitch.channel.permissions.UserPermission;

/**
 * Created by Dominic Hauton on 28/03/2016.
 *
 * Interface to allow easy manipulation of TwitchCommands
 */
public interface TwitchCommand {
    /**
     * Checks if the given user permission is sufficient to execute the command.
     * @param userPermission The permission the user has.
     * @return true if the user has the required permission or higher.
     */
    boolean hasRequiredPermission(UserPermission userPermission);

    /**
     * Runs a command. This results in a CommandResponse
     * @param twitchMessage
     * @return
     */
    CommandResponse runCommand(TwitchMessage twitchMessage);
}
