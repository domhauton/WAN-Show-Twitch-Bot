package twitch.channel.blacklist;

import java.util.regex.Pattern;

/**
 * Created by Dominic Hauton on 03/05/2016.
 * <p>
 * Stores a blacklist pattern.
 */
public class BlacklistEntry {
    private final Pattern m_pattern;

    BlacklistEntry(Pattern pattern) {
        this.m_pattern = pattern;
    }

    public boolean matches(String inputString) {
        return m_pattern.matcher(inputString).matches();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BlacklistEntry)) { return false; }
        BlacklistEntry that = (BlacklistEntry) o;
        return toString().equals((that.toString()));
    }

    @Override
    public int hashCode() {
        return m_pattern.hashCode();
    }

    @Override
    public String toString() {
        return m_pattern.pattern();
    }
}
