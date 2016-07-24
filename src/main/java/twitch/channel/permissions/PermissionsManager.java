package twitch.channel.permissions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitch.channel.TwitchUser;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Dominic Hauton on 12/03/2016.
 *
 * Contains the permissions for all of the users.
 */
public class PermissionsManager {
    private ConcurrentHashMap<TwitchUser, UserPermission> userHashMap;
    private static final Logger s_log = LogManager.getLogger();

    public PermissionsManager() {
        userHashMap = new ConcurrentHashMap<>();
    }

    /**
     * Gets the current permissions for the given user.
     * @return The stored permissions or default.
     */
    public UserPermission getUser(TwitchUser user) {
        s_log.debug("Retrieving permissions for user {} from Permissions Manager.", user::getUsername);
        return userHashMap.getOrDefault(user, UserPermission.getDefaultPermission());
    }

    /**
     * Adds/Replaces any user stored
     */
    public void changeUserPermission(TwitchUser user, UserPermission userPermission){
        if(userPermission == UserPermission.getDefaultPermission()) {
            userHashMap.remove(user);
        } else {
            userHashMap.put(user, userPermission);
        }
    }
}
