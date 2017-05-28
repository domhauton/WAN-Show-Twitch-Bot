package com.domhauton.wanbot.bot.channel.blacklist;

import com.domhauton.wanbot.bot.channel.TwitchUser;
import com.domhauton.wanbot.bot.channel.message.TwitchMessage;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Dominic Hauton on 13/07/2016.
 * <p>
 * Tests the blacklist manager to see if messages are correctly blacklisted.
 */
class BlacklistManagerTest {
  private static final String channelName = "channelFooBar";
  private static final TwitchUser mockUser = new TwitchUser("userFooBar");
  private static final TwitchMessage message1 = new TwitchMessage("fooBar", mockUser, DateTime.now(),
      channelName);
  private static final TwitchMessage message2 = new TwitchMessage("fooBa", mockUser, DateTime.now(),
      channelName);
  private static String exampleWord = "foobar123";

  private BlacklistManager blacklistManager;

  @BeforeEach
  void setUp() throws Exception {
    blacklistManager = new BlacklistManager();
  }

  @Test
  void addToBlacklistMessage() throws Exception {
    Assertions.assertFalse(blacklistManager.isMessageBlacklisted(message1.getMessage()), "Message should not be blacklisted yet");
    blacklistManager.addToBlacklist(message1.getMessage(), BlacklistType.MESSAGE);
    Assertions.assertTrue(blacklistManager.isMessageBlacklisted(message1.getMessage()), "Message should now been blacklisted");
    Assertions.assertFalse(blacklistManager.isMessageBlacklisted(message2.getMessage()), "Message should not now been blacklisted");
  }

  @Test
  void addTwoMessagesToBlacklist() throws Exception {
    addToBlacklistMessage();
    blacklistManager.addToBlacklist(message2.getMessage(), BlacklistType.MESSAGE);
    Assertions.assertTrue(blacklistManager.isMessageBlacklisted(message2.getMessage()), "Message should now been blacklisted");
    Assertions.assertTrue(blacklistManager.isMessageBlacklisted(message1.getMessage()), "Message 1 is should still be blacklisted");
  }

  @Test
  void removeMessageFromBlacklist() throws Exception {
    addTwoMessagesToBlacklist();
    blacklistManager.removeFromBlacklist(message2.getMessage(), BlacklistType.MESSAGE);
    Assertions.assertFalse(blacklistManager.isMessageBlacklisted(message2.getMessage()), "Message 2 should no longer be blacklisted");
    Assertions.assertTrue(blacklistManager.isMessageBlacklisted(message1.getMessage()), "Message 1 should still blacklisted");
  }

  @Test
  void removeMessageFromBlacklistDoesNotExistException() throws Exception {
    addToBlacklistMessage();
    Assertions.assertThrows(BlacklistOperationException.class,
        () -> blacklistManager.removeFromBlacklist(message2.getMessage(), BlacklistType.MESSAGE));
  }

  @Test
  void searchForExistingBlacklistEntriesTest() throws Exception {
    addToBlacklistMessage();
    Collection<BlacklistEntry> blacklistEntries = blacklistManager.searchBlacklist(message2.getMessage());
    Assertions.assertFalse(blacklistEntries.isEmpty(), "Should find at least one entry");
    Assertions.assertEquals(1, blacklistEntries.size(), "Only 1 blacklistEntry should return");
  }

  @Test
  void searchForExistingBlacklistEntriesTwoResultsTest() throws Exception {
    addTwoMessagesToBlacklist();
    Collection<BlacklistEntry> blacklistEntries = blacklistManager.searchBlacklist(message2.getMessage());
    Assertions.assertFalse(blacklistEntries.isEmpty(), "Should find at least one entry");
    Assertions.assertEquals(2, blacklistEntries.size(), "2 blacklistEntries should return");
  }

  @Test
  void searchForExistingBlacklistEntriesNoResultsTest() throws Exception {
    addToBlacklistMessage();
    Collection<BlacklistEntry> blacklistEntries = blacklistManager.searchBlacklist(message1.getMessage() + " ");
    Assertions.assertTrue(blacklistEntries.isEmpty(), "Should find no entries");
  }

  @Test
  void searchForExistingBlacklistEntryAndRemoveTest() throws Exception {
    addToBlacklistMessage();
    Collection<BlacklistEntry> blacklistEntries = blacklistManager.searchBlacklist(message2.getMessage());
    Assertions.assertFalse(blacklistEntries.isEmpty(), "Should find at least one entry");
    Assertions.assertEquals(1, blacklistEntries.size(), "Only 1 blacklistEntry should return");
    BlacklistEntry blacklistEntry = blacklistEntries.iterator().next(); // TOCTOU vulnerable but only a test.
    blacklistManager.removeFromBlacklist(blacklistEntry);
    Assertions.assertFalse(blacklistManager.isMessageBlacklisted(message1.getMessage()), "Message should no longer be blacklisted");
  }

  @Test
  void messageOnlyBlacklistTest() throws Exception {
    blacklistManager.addToBlacklist(message2.getMessage(), BlacklistType.MESSAGE);
    Assertions.assertFalse(blacklistManager.isMessageBlacklisted(message1.getMessage()),
        "Should not match message containing part of blacklisted message");
    Assertions.assertTrue(blacklistManager.isMessageBlacklisted(message2.getMessage()),
        "Should match exact message containing part of blacklisted message");
  }

  @Test
  void wordBlacklistTest() throws Exception {
    blacklistManager.addToBlacklist(message2.getMessage(), BlacklistType.WORD);
    Assertions.assertTrue(blacklistManager.isMessageBlacklisted(message1.getMessage()),
        "Should match message containing part of blacklisted word");
    Assertions.assertTrue(blacklistManager.isMessageBlacklisted(message2.getMessage()),
        "Should match exact message containing part of blacklisted word");
  }

  @Test
  void regexBlacklistTest() throws Exception {
    blacklistManager.addToBlacklist(".*", BlacklistType.REGEX);
    Assertions.assertTrue(blacklistManager.isMessageBlacklisted(""), "Should have banned everything.");
    Collection<BlacklistEntry> blacklistEntries = blacklistManager.searchBlacklist("");
    blacklistEntries.forEach(blacklistEntry -> removeFromBlacklistIgnoreException(blacklistManager, blacklistEntry));
    Assertions.assertFalse(blacklistManager.isMessageBlacklisted(""),
        "Everything should be unbanned. Testing for fat finger recourse.");
  }

  private void removeFromBlacklistIgnoreException(BlacklistManager blacklistManager, BlacklistEntry blacklistEntry) {
    try {
      blacklistManager.removeFromBlacklist(blacklistEntry);
    } catch (BlacklistOperationException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void removeFromBlacklistFuzzySimpleTest() throws Exception {

    Collection<BlacklistEntry> expectedBlacklistEntries = Stream.of(BlacklistType.values())
        .map(type -> addToBlacklistUnsafe(exampleWord, type))
        .collect(Collectors.toSet());
    Assertions.assertTrue(blacklistManager.isMessageBlacklisted(exampleWord), "Should be blacklisted");
    Collection<BlacklistEntry> actualRemovedEntries = blacklistManager.removeFromBlacklist(exampleWord);
    Assertions.assertFalse(blacklistManager.isMessageBlacklisted(exampleWord), "Should not be blacklisted");
    Assertions.assertEquals(expectedBlacklistEntries, actualRemovedEntries, "Should have removed all 3 original entries");
  }

  @Test
  void removeFromBlacklistFuzzySearchTest() throws Exception {
    Collection<BlacklistEntry> expectedBlacklistEntries = Stream.of(BlacklistType.values())
        .map(type -> addToBlacklistUnsafe(exampleWord, type))
        .collect(Collectors.toSet());
    Assertions.assertTrue(blacklistManager.isMessageBlacklisted(exampleWord), "Should be blacklisted");
    Collection<BlacklistEntry> actualRemovedEntries = blacklistManager.removeFromBlacklist(exampleWord
        .substring(2, 4));
    Assertions.assertFalse(blacklistManager.isMessageBlacklisted(exampleWord), "Should not be blacklisted");
    Assertions.assertEquals(expectedBlacklistEntries, actualRemovedEntries, "Should have removed all 3 original entries");
  }

  @Test
  void removeFromBlacklistOnlyExactMatchTest() throws Exception {
    Collection<BlacklistEntry> expectedBlacklistEntries = Stream.of(BlacklistType.values())
        .map(type -> addToBlacklistUnsafe(exampleWord, type))
        .collect(Collectors.toSet());
    addToBlacklistUnsafe(exampleWord.substring(1), BlacklistType.MESSAGE);
    Assertions.assertTrue(blacklistManager.isMessageBlacklisted(exampleWord), "Should be blacklisted");
    Collection<BlacklistEntry> actualRemovedEntries = blacklistManager.removeFromBlacklist(exampleWord);
    Assertions.assertFalse(blacklistManager.isMessageBlacklisted(exampleWord), "Should not be blacklisted");
    Assertions.assertTrue(blacklistManager.isMessageBlacklisted(exampleWord.substring(1)),
        "Should still be blacklisted");
    Assertions.assertEquals(expectedBlacklistEntries, actualRemovedEntries, "Should have removed all 3 original entries");
  }

  @Test
  void removeFromBlacklistFuzzyNoneMatchTest() throws Exception {
    Collection<BlacklistEntry> blacklistEntries = blacklistManager.removeFromBlacklist("foobar");
    Assertions.assertTrue(blacklistEntries.isEmpty(), "No items in blacklist. Should be empty");
  }

  /**
   * Test only addition method for streams.
   */
  private BlacklistEntry addToBlacklistUnsafe(String message, BlacklistType type) {
    try {
      return blacklistManager.addToBlacklist(message, type);
    } catch (BlacklistOperationException e) {
      throw new RuntimeException(e);
    }
  }
}