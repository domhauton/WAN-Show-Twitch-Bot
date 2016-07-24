package twitch.channel.permissions;

/**
 * Created by Dominic Hauton on 11/03/2016.
 *
 * Controls user permissions
 */
public enum UserPermission {
    ChannelOwner(0),
    BotAdmin(1),
    BotModerator(2),
    ChannelModerator(3),
    ChannelUser(4);

    private int permissionLevel;

    UserPermission(int permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    public static UserPermission getDefaultPermission() {
        return UserPermission.ChannelUser;
    }

    /**
     * Checks if the current permissions have permissions for the level given.
     * @param requiredPermissionLevel Required permissions level for action.
     * @return true if has permission required or higher.
     */
    public boolean authorizedForActionOfPermissionLevel(UserPermission requiredPermissionLevel) {
        return requiredPermissionLevel.permissionLevel >= permissionLevel;
    }
}
