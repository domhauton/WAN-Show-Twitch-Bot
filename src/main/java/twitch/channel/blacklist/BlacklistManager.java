package twitch.channel.blacklist;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitch.channel.data.TwitchMessage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Dominic Hauton on 03/05/2016.
 *
 * Stores the channel blacklists
 */
public class BlacklistManager {
    private static final Logger s_log = LogManager.getLogger();
    private Set<BlacklistEntry> m_blacklistEntries;

    public BlacklistManager() {
        this.m_blacklistEntries = new HashSet<>();
    }

    /**
     * Attempts to add pattern to blacklist. Rejected if already blacklisted.
     */
    public BlacklistEntry addToBlacklist(String input, BlacklistType blacklistType) throws
            BlacklistOperationOperationException {
        Pattern convertedPatten = blacklistType.stringToPattern(input);
        BlacklistEntry blacklistEntry = new BlacklistEntry(convertedPatten);
        if(!m_blacklistEntries.contains(blacklistEntry)) {
            s_log.warn("Attempted to add existing pattern to blacklist: {}", convertedPatten);
            throw new BlacklistOperationOperationException("Pattern attempted: " + convertedPatten.toString());
        } else {
            m_blacklistEntries.add(blacklistEntry);
            s_log.info("Added blacklist pattern: {}", convertedPatten);
            return blacklistEntry;
        }
    }

    /**
     * Add input to blacklist and keep messages that match.
     * @return Messages that match new rule.
     */
    public Collection<TwitchMessage> addToBlacklist(
            String input,
            BlacklistType blacklistType,
            Collection<TwitchMessage> twitchMessages) throws BlacklistOperationOperationException {
        BlacklistEntry blacklistEntry = addToBlacklist(input, blacklistType);
        Collection<TwitchMessage> filteredMessages = twitchMessages.stream()
                .filter(twitchMessage -> blacklistEntry.matches(twitchMessage.getMessage()))
                .collect(Collectors.toList());
        s_log.info("Found {} of {} messages matching new rule.", twitchMessages::size, filteredMessages::size);
        return filteredMessages;
    }

    public void removeFromBlacklist(String input, BlacklistType blacklistType) throws BlacklistOperationOperationException {
        Pattern convertedPatten = blacklistType.stringToPattern(input);
        BlacklistEntry blacklistEntry = new BlacklistEntry(convertedPatten);
        if(m_blacklistEntries.contains(blacklistEntry)) {
            m_blacklistEntries.remove(blacklistEntry);
        } else {
            throw new BlacklistOperationOperationException("Could not remove pattern. Pattern not found: " + convertedPatten);
        }
    }
}
