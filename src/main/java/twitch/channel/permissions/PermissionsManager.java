package twitch.channel.permissions;

import twitch.channel.TwitchUser;

import java.util.HashMap;

/**
 * Created by Dominic Hauton on 12/03/2016.
 *
 * Contains the permissions for all of the users.
 */
public class PermissionsManager {
    private HashMap<TwitchUser, UserPermission> userHashMap;

    public PermissionsManager() {
        userHashMap = new HashMap<>();
    }

    /**
     * Gets the current permissions for the given user.
     * @return The stored permissions or default.
     */
    public UserPermission getUser(TwitchUser user) {
        return userHashMap.getOrDefault(user, UserPermission.getDefaultPermission());
    }

    /**
     * Adds/Replaces any user stored
     * @return The previous value or default.
     */
    public UserPermission addUser(TwitchUser user, UserPermission userPermission){
        UserPermission oldPermission = userHashMap.put(user, userPermission);
        return oldPermission == null ? UserPermission.getDefaultPermission() : oldPermission;
    }
}
