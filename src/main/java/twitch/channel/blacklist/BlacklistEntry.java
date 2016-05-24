package twitch.channel.blacklist;

import com.google.common.base.Objects;

import java.util.regex.Pattern;

/**
 * Created by Dominic Hauton on 03/05/2016.
 *
 * Stores a blacklist pattern.
 */
public class BlacklistEntry {
    private Pattern m_pattern;

    BlacklistEntry(Pattern pattern) {
        this.m_pattern = pattern;
    }

    public boolean matches(String inputString){
        return m_pattern.matcher(inputString).matches();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof BlacklistEntry)) { return false; }
        BlacklistEntry that = (BlacklistEntry) o;
        return Objects.equal(m_pattern, that.m_pattern);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(m_pattern);
    }

    @Override
    public String toString() {
        return m_pattern.toString();
    }
}
