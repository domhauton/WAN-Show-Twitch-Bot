package twitch.channel.blacklist;

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
}
