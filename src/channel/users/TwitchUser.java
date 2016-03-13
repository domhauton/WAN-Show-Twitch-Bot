package channel.users;

import java.util.Objects;

/**
 * Created by Dominic Hauton on 11/03/2016.
 */
public class TwitchUser {
    private String username;
    private UserPermission userPermission;

    public TwitchUser(String username, UserPermission userPermission) {
        Objects.requireNonNull(username);
        this.username = username;
        this.userPermission = userPermission;
    }

    public String getUsername() {
        return username;
    }

    /**
     * Checks if the username of the user is the same as the given string.
     */
    public boolean equalsUsername(String username) {
        return this.getUsername().equalsIgnoreCase(username);
    }

    public UserPermission getUserPermission() {
        return userPermission;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TwitchUser that = (TwitchUser) o;

        return username != null ? username.equals(that.username) : that.username == null;

    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }
}
