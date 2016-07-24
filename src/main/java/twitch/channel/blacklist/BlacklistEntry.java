package twitch.channel.blacklist;

import java.util.regex.Pattern;

/**
 * Created by Dominic Hauton on 03/05/2016.
 * <p>
 * Stores a blacklist pattern.
 */
public class BlacklistEntry {
    private final Pattern m_pattern;
    private final BlacklistType m_blacklistType;

    BlacklistEntry(Pattern pattern) {
        this(pattern, BlacklistType.REGEX);
    }

    BlacklistEntry(Pattern pattern, BlacklistType blacklistType) {
        m_pattern = pattern;
        m_blacklistType = blacklistType;
    }

    public boolean matches(String inputString) {
        return m_pattern.matcher(inputString).matches();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BlacklistEntry)) { return false; }
        BlacklistEntry that = (BlacklistEntry) o;
        // Do not compare blacklist type, unnecessary!
        return toString().equals(that.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return m_pattern.pattern();
    }

    BlacklistType getBlacklistType() {
        return m_blacklistType;
    }
}
