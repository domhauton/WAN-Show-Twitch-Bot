package twitch.channel.blacklist;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitch.channel.message.TwitchMessage;

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
    private final Set<BlacklistEntry> m_blacklistEntries;

    public BlacklistManager() {
        m_blacklistEntries = new HashSet<>();
    }

    /**
     * Attempts to add pattern to blacklist. Rejected if already blacklisted.
     */
    public BlacklistEntry addToBlacklist(String input, BlacklistType blacklistType) throws
            BlacklistOperationOperationException {
        Pattern convertedPatten = blacklistType.stringToPattern(input);
        BlacklistEntry blacklistEntry = new BlacklistEntry(convertedPatten);
        if(m_blacklistEntries.contains(blacklistEntry)) {
            s_log.warn("Failed to add pattern. Already existed: {}", convertedPatten);
            throw new BlacklistOperationOperationException("Pattern attempted: " + convertedPatten.toString());
        } else {
            m_blacklistEntries.add(blacklistEntry);
            s_log.info("Added blacklist pattern: {} as: {}", convertedPatten, blacklistType);
            return blacklistEntry;
        }
    }

    /**
     * Check message against all blacklist entries.
     */
    public boolean isMessageBlacklisted(String twitchMessage) {
        return m_blacklistEntries
                .stream()
                .filter(blacklistEntry -> blacklistEntry.matches(twitchMessage))
                .findAny()
                .isPresent();
    }

    public void removeFromBlacklist(String input, BlacklistType blacklistType) throws BlacklistOperationOperationException {
        Pattern convertedPatten = blacklistType.stringToPattern(input);
        BlacklistEntry blacklistEntry = new BlacklistEntry(convertedPatten);
        if(m_blacklistEntries.contains(blacklistEntry)) {
            s_log.info("Removing blacklist pattern {} which is a {}", convertedPatten, blacklistType);
            m_blacklistEntries.remove(blacklistEntry);
        } else {
            s_log.warn(
                    "Failed to remove pattern as it could not be found.\nRemoving:\n{}\nExist:\n{}",
                    () -> blacklistEntry,
                    () -> m_blacklistEntries.stream().map(BlacklistEntry::toString).collect(Collectors.joining("\n")));
            throw new BlacklistOperationOperationException("Could not remove pattern. Pattern not found: " + convertedPatten);
        }
    }

    public Collection<String> searchBlacklist(String searchTerm) {
        return m_blacklistEntries
                .stream()
                .map(BlacklistEntry::toString)
                .filter(pattern -> pattern.contains(searchTerm))
                .collect(Collectors.toSet());
    }
}
