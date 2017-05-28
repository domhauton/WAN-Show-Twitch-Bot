package com.domhauton.wanbot.bot.channel.blacklist;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Dominic Hauton on 03/05/2016.
 * <p>
 * Stores the channel blacklists
 */
public class BlacklistManager {
  private static final Logger log = LogManager.getLogger();
  private final Set<BlacklistEntry> blacklistEntries;

  public BlacklistManager() {
    blacklistEntries = new HashSet<>();
  }

  /**
   * Attempts to add pattern to blacklist. Rejected if already blacklisted.
   */
  public BlacklistEntry addToBlacklist(String input, BlacklistType blacklistType) throws BlacklistOperationException {
    Pattern convertedPatten = blacklistType.stringToPattern(input);
    BlacklistEntry blacklistEntry = new BlacklistEntry(convertedPatten, blacklistType);
    if (blacklistEntries.contains(blacklistEntry)) {
      log.warn("Failed to add pattern. Already existed: {}", convertedPatten);
      throw new BlacklistOperationException("Failed to add pattern. Already existed: " +
          convertedPatten.toString());
    } else {
      blacklistEntries.add(blacklistEntry);
      log.info("Added blacklist pattern: {} as: {}", convertedPatten, blacklistType);
      return blacklistEntry;
    }
  }

  /**
   * Check message against all blacklist entries.
   */
  public boolean isMessageBlacklisted(String twitchMessage) {
    log.debug("Checking if message is blacklisted: {}", twitchMessage);
    return blacklistEntries.stream()
        .filter(blacklistEntry -> blacklistEntry.matches(twitchMessage))
        .findAny()
        .isPresent();
  }

  public BlacklistEntry removeFromBlacklist(String input, BlacklistType blacklistType) throws BlacklistOperationException {
    Pattern convertedPatten = blacklistType.stringToPattern(input);
    BlacklistEntry blacklistEntry = new BlacklistEntry(convertedPatten);
    return removeFromBlacklist(blacklistEntry);
  }

  BlacklistEntry removeFromBlacklist(BlacklistEntry blacklistEntry) throws BlacklistOperationException {
    if (blacklistEntries.contains(blacklistEntry)) {
      log.info("Removing blacklist pattern {} which is a {}", blacklistEntry.toString(), blacklistEntry.getBlacklistType()
          .toString());
      blacklistEntries.remove(blacklistEntry);
      return blacklistEntry;
    } else {
      log.info("Attempted to remove non-existent blacklist entry {} . Current entries: {}", () ->
          blacklistEntry, () -> blacklistEntries
          .stream()
          .map(BlacklistEntry::toString)
          .collect(Collectors.toList())
          .toString());
      throw new BlacklistOperationException(
          "Could not remove pattern. Pattern not found: " + blacklistEntry.toString());
    }
  }

  /**
   * An unsafe way to remove from blacklist, will return null instead of throwing exception. For
   * streams.
   *
   * @return null if remove failed. Otherwise item removed.
   */
  private BlacklistEntry removeFromBlacklistUnsafe(String input, BlacklistType blacklistType) {
    try {
      return removeFromBlacklist(input, blacklistType);
    } catch (BlacklistOperationException e) {
      return null;
    }
  }

  /**
   * An unsafe way to remove from blacklist, will return null instead of throwing exception. For
   * streams.
   *
   * @return null if remove failed. Otherwise item removed.
   */
  private BlacklistEntry removeFromBlacklistUnsafe(BlacklistEntry blacklistEntry) {
    try {
      return removeFromBlacklist(blacklistEntry);
    } catch (BlacklistOperationException e) {
      // Race condition precaution
      return null;
    }
  }

  public Collection<BlacklistEntry> removeFromBlacklist(String input) {
    log.info("Attempting assisted blacklist removal of: {}.", input);
    Collection<BlacklistEntry> removedBlacklistEntries = Stream.of(BlacklistType.values())
        .map(blacklistType -> removeFromBlacklistUnsafe(input, blacklistType))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    if (removedBlacklistEntries.isEmpty()) {
      log.info("Attempting assisted blacklist removal of: {}. No exact match found, searching...", input);
      Collection<BlacklistEntry> matchingEntries = searchBlacklist(input);
      Collection<BlacklistEntry> removedSearchEntries = matchingEntries.stream()
          .map(this::removeFromBlacklistUnsafe)
          .filter(Objects::nonNull)
          .collect(Collectors.toSet());
      return removedSearchEntries;
    } else {
      log.info("Attempting assisted blacklist removal of: {}. Found exact match/es.", input);
      return removedBlacklistEntries;
    }
  }

  public Collection<BlacklistEntry> searchBlacklist(String searchTerm) {
    log.info("Searching blacklist for: {}", searchTerm);
    return blacklistEntries.stream()
        .filter(blacklistEntry -> blacklistEntry.toString().contains(searchTerm))
        .collect(Collectors.toSet());
  }
}
