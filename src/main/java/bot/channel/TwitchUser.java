package bot.channel;

import java.util.Objects;

/**
 * Created by Dominic Hauton on 11/03/2016.
 *
 * Decorator for String to give TwitchUser functions
 */
public class TwitchUser {
    private String username;

    public TwitchUser(String username) {
        Objects.requireNonNull(username);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TwitchUser)) return false;
        TwitchUser that = (TwitchUser) o;
        return com.google.common.base.Objects.equal(getUsername(), that.getUsername());
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(getUsername());
    }

    @Override
    public String toString() {
        return username;
    }
}
