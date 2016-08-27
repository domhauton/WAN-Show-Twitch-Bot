package bot.channel.blacklist;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import bot.channel.TwitchUser;
import bot.channel.message.TwitchMessage;

/**
 * Created by Dominic Hauton on 13/07/2016.
 * <p>
 * Tests the blacklist manager to see if messages are correctly blacklisted.
 */
public class BlacklistManagerTest {
  private static final String channelName = "channelFooBar";
  private static final TwitchUser mockUser = new TwitchUser("userFooBar");
  private static final TwitchMessage message1 = new TwitchMessage("fooBar", mockUser, DateTime.now(),
      channelName);
  private static final TwitchMessage message2 = new TwitchMessage("fooBa", mockUser, DateTime.now(),
      channelName);
  private static String exampleWord = "foobar123";
  @Rule
  public ExpectedException thrown = ExpectedException.none();
  private BlacklistManager blacklistManager;

  @Before
  public void setUp() throws Exception {
    blacklistManager = new BlacklistManager();
  }

  @Test
  public void addToBlacklistMessage() throws Exception {
    Assert.assertFalse("Message should not be blacklisted yet", blacklistManager.isMessageBlacklisted(message1.getMessage()));
    blacklistManager.addToBlacklist(message1.getMessage(), BlacklistType.MESSAGE);
    Assert.assertTrue("Message should now been blacklisted", blacklistManager.isMessageBlacklisted(message1.getMessage()));
    Assert.assertFalse("Message should not now been blacklisted", blacklistManager.isMessageBlacklisted(message2.getMessage()));
  }

  @Test
  public void addTwoMessagesToBlacklist() throws Exception {
    addToBlacklistMessage();
    blacklistManager.addToBlacklist(message2.getMessage(), BlacklistType.MESSAGE);
    Assert.assertTrue("Message should now been blacklisted", blacklistManager.isMessageBlacklisted(message2.getMessage()));
    Assert.assertTrue("Message 1 is should still be blacklisted", blacklistManager.isMessageBlacklisted(message1.getMessage()));
  }

  @Test
  public void removeMessageFromBlacklist() throws Exception {
    addTwoMessagesToBlacklist();
    blacklistManager.removeFromBlacklist(message2.getMessage(), BlacklistType.MESSAGE);
    Assert.assertFalse("Message 2 should no longer be blacklisted", blacklistManager.isMessageBlacklisted(message2
        .getMessage()));
    Assert.assertTrue("Message 1 should still blacklisted", blacklistManager.isMessageBlacklisted(message1.getMessage()));
  }

  @Test
  public void removeMessageFromBlacklistDoesNotExistException() throws Exception {
    addToBlacklistMessage();
    thrown.expect(BlacklistOperationException.class);
    thrown.reportMissingExceptionWithMessage(
        "Exception should be thrown. Should not be able to remove from " + "blacklist.");
    blacklistManager.removeFromBlacklist(message2.getMessage(), BlacklistType.MESSAGE);
  }

  @Test
  public void searchForExistingBlacklistEntriesTest() throws Exception {
    addToBlacklistMessage();
    Collection<BlacklistEntry> blacklistEntries = blacklistManager.searchBlacklist(message2.getMessage());
    Assert.assertFalse("Should find at least one entry", blacklistEntries.isEmpty());
    Assert.assertEquals("Only 1 blacklistEntry should return", 1, blacklistEntries.size());
  }

  @Test
  public void searchForExistingBlacklistEntriesTwoResultsTest() throws Exception {
    addTwoMessagesToBlacklist();
    Collection<BlacklistEntry> blacklistEntries = blacklistManager.searchBlacklist(message2.getMessage());
    Assert.assertFalse("Should find at least one entry", blacklistEntries.isEmpty());
    Assert.assertEquals("2 blacklistEntries should return", 2, blacklistEntries.size());
  }

  @Test
  public void searchForExistingBlacklistEntriesNoResultsTest() throws Exception {
    addToBlacklistMessage();
    Collection<BlacklistEntry> blacklistEntries = blacklistManager.searchBlacklist(message1.getMessage() + " ");
    Assert.assertTrue("Should find no entries", blacklistEntries.isEmpty());
  }

  @Test
  public void searchForExistingBlacklistEntryAndRemoveTest() throws Exception {
    addToBlacklistMessage();
    Collection<BlacklistEntry> blacklistEntries = blacklistManager.searchBlacklist(message2.getMessage());
    Assert.assertFalse("Should find at least one entry", blacklistEntries.isEmpty());
    Assert.assertEquals("Only 1 blacklistEntry should return", 1, blacklistEntries.size());
    BlacklistEntry blacklistEntry = blacklistEntries.iterator().next(); // TOCTOU vulnerable but only a test.
    blacklistManager.removeFromBlacklist(blacklistEntry);
    Assert.assertFalse("Message should no longer be blacklisted", blacklistManager.isMessageBlacklisted(message1.getMessage()));
  }

  @Test
  public void messageOnlyBlacklistTest() throws Exception {
    blacklistManager.addToBlacklist(message2.getMessage(), BlacklistType.MESSAGE);
    Assert.assertFalse("Should not match message containing part of blacklisted message", blacklistManager.isMessageBlacklisted(message1
        .getMessage()));
    Assert.assertTrue("Should match exact message containing part of blacklisted message", blacklistManager
        .isMessageBlacklisted(message2.getMessage()));
  }

  @Test
  public void wordBlacklistTest() throws Exception {
    blacklistManager.addToBlacklist(message2.getMessage(), BlacklistType.WORD);
    Assert.assertTrue("Should match message containing part of blacklisted word", blacklistManager
        .isMessageBlacklisted(message1
            .getMessage()));
    Assert.assertTrue("Should match exact message containing part of blacklisted word", blacklistManager
        .isMessageBlacklisted(message2.getMessage()));
  }

  @Test
  public void regexBlacklistTest() throws Exception {
    blacklistManager.addToBlacklist(".*", BlacklistType.REGEX);
    Assert.assertTrue("Should have banned everything.", blacklistManager.isMessageBlacklisted(""));
    Collection<BlacklistEntry> blacklistEntries = blacklistManager.searchBlacklist("");
    blacklistEntries.forEach(blacklistEntry -> removeFromBlacklistIgnoreException(blacklistManager, blacklistEntry));
    Assert.assertFalse("Everything should be unbanned. Testing for fat finger recourse.", blacklistManager
        .isMessageBlacklisted(""));
  }

  private void removeFromBlacklistIgnoreException(BlacklistManager blacklistManager, BlacklistEntry blacklistEntry) {
    try {
      blacklistManager.removeFromBlacklist(blacklistEntry);
    } catch (BlacklistOperationException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void removeFromBlacklistFuzzySimpleTest() throws Exception {

    Collection<BlacklistEntry> expectedBlacklistEntries = Stream.of(BlacklistType.values())
        .map(type -> addToBlacklistUnsafe(exampleWord, type))
        .collect(Collectors.toSet());
    Assert.assertTrue("Should be blacklisted", blacklistManager.isMessageBlacklisted(exampleWord));
    Collection<BlacklistEntry> actualRemovedEntries = blacklistManager.removeFromBlacklist(exampleWord);
    Assert.assertFalse("Should not be blacklisted", blacklistManager.isMessageBlacklisted(exampleWord));
    Assert.assertEquals("Should have removed all 3 original entries", expectedBlacklistEntries, actualRemovedEntries);
  }

  @Test
  public void removeFromBlacklistFuzzySearchTest() throws Exception {
    Collection<BlacklistEntry> expectedBlacklistEntries = Stream.of(BlacklistType.values())
        .map(type -> addToBlacklistUnsafe(exampleWord, type))
        .collect(Collectors.toSet());
    Assert.assertTrue("Should be blacklisted", blacklistManager.isMessageBlacklisted(exampleWord));
    Collection<BlacklistEntry> actualRemovedEntries = blacklistManager.removeFromBlacklist(exampleWord
        .substring(2, 4));
    Assert.assertFalse("Should not be blacklisted", blacklistManager.isMessageBlacklisted(exampleWord));
    Assert.assertEquals("Should have removed all 3 original entries", expectedBlacklistEntries, actualRemovedEntries);
  }

  @Test
  public void removeFromBlacklistOnlyExactMatchTest() throws Exception {
    Collection<BlacklistEntry> expectedBlacklistEntries = Stream.of(BlacklistType.values())
        .map(type -> addToBlacklistUnsafe(exampleWord, type))
        .collect(Collectors.toSet());
    addToBlacklistUnsafe(exampleWord.substring(1), BlacklistType.MESSAGE);
    Assert.assertTrue("Should be blacklisted", blacklistManager.isMessageBlacklisted(exampleWord));
    Collection<BlacklistEntry> actualRemovedEntries = blacklistManager.removeFromBlacklist(exampleWord);
    Assert.assertFalse("Should not be blacklisted", blacklistManager.isMessageBlacklisted(exampleWord));
    Assert.assertTrue("Should still be blacklisted", blacklistManager.isMessageBlacklisted(exampleWord
        .substring(1)));
    Assert.assertEquals("Should have removed all 3 original entries", expectedBlacklistEntries, actualRemovedEntries);
  }

  @Test
  public void removeFromBlacklistFuzzyNoneMatchTest() throws Exception {
    Collection<BlacklistEntry> blacklistEntries = blacklistManager.removeFromBlacklist("foobar");
    Assert.assertTrue("No items in blacklist. Should be empty", blacklistEntries.isEmpty());
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