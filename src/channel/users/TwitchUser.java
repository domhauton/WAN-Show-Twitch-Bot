package channel.users;

import java.util.Objects;

/**
 * Created by Dominic Hauton on 11/03/2016.
 *
 * Data class for Twitch Users
 */
public class TwitchUser {
    private String username;
    private UserPermission userPermission;

    public TwitchUser(String username, UserPermission userPermission) {
        Objects.requireNonNull(username);
        this.username = username;
        this.userPermission = userPermission;
    }

    /**
     * Constructor that defaults the user permission to ChannelUser
     * @param username
     */
    public TwitchUser(String username) {
        this(username, UserPermission.ChannelUser);
    }

    public String getUsername() {
        return username;
    }

    public UserPermission getUserPermission() {
        return userPermission;
    }

    public boolean hasRequiredPermission(UserPermission requiredPermission) {
        return getUserPermission().hasRequiredPermissions(requiredPermission);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TwitchUser)) return false;

        TwitchUser that = (TwitchUser) o;

        if (getUsername() != null ? !getUsername().equals(that.getUsername()) : that.getUsername() != null)
            return false;
        return getUserPermission() == that.getUserPermission();

    }

    @Override
    public int hashCode() {
        int result = getUsername() != null ? getUsername().hashCode() : 0;
        result = 31 * result + (getUserPermission() != null ? getUserPermission().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", username, getUserPermission());
    }
}
