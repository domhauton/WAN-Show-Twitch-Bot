package twitch.channel.blacklist;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by Dominic Hauton on 03/05/2016.
 *
 * Stores the channel blacklists
 */
public class BlacklistManager {
    private Set<BlacklistEntry> m_blacklistEntries;

    public BlacklistManager() {
        this.m_blacklistEntries = new HashSet<>();
    }

    public void addBlackListWord(String word){
        String literalPattern = "*"+Pattern.quote(word)+"*";
        blacklistRegex(literalPattern);
    }

    public void blacklistMessage(String message){
        String literalPattern = Pattern.quote(message);
        blacklistRegex(literalPattern);
    }

    private void blacklistRegex(String string){
        Pattern pattern = Pattern.compile(string, Pattern.CASE_INSENSITIVE);
        BlacklistEntry blacklistEntry = new BlacklistEntry(pattern);
        m_blacklistEntries.add(blacklistEntry);
    }

    public Collection<BlacklistEntry> getBlacklist(){
        return ImmutableList.copyOf(m_blacklistEntries);
    }
}
