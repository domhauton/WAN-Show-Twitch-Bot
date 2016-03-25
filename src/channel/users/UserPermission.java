package channel.users;

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

    public int getPermissionLevel() {
        return permissionLevel;
    }

    public static UserPermission getDefaultPermission() {
        return UserPermission.ChannelUser;
    }

    public boolean hasRequiredPermissions(UserPermission requiredPermissionLevel) {
        return requiredPermissionLevel.getPermissionLevel() >= getPermissionLevel();
    }
}
