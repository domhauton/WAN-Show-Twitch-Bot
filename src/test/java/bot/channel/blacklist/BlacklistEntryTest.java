package bot.channel.blacklist;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

/**
 * Created by Dominic Hauton on 12/07/2016.
 *
 * Test for a blacklist entry
 */
class BlacklistEntryTest {
  static final String exampleWord = "foobar";

  BlacklistEntry blacklistEntryMatchExact;
  BlacklistEntry blacklistEntryContainsWord;
  BlacklistEntry blacklistEntryMatchAll;

  @BeforeEach
  void setUp() throws Exception {
    Pattern matchAll = Pattern.compile("^.*$");
    blacklistEntryMatchAll = new BlacklistEntry(matchAll);
    Pattern matchContainsWord = Pattern.compile("^.*" + exampleWord + ".*$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    blacklistEntryContainsWord = new BlacklistEntry(matchContainsWord);
    Pattern matchExact = Pattern.compile("^" + exampleWord + "$");
    blacklistEntryMatchExact = new BlacklistEntry(matchExact);
  }

  @Test
  void matchesExactTest() throws Exception {
    Assertions.assertTrue(blacklistEntryMatchExact.matches(exampleWord), "Should match exact MESSAGE.");
    Assertions.assertFalse(blacklistEntryMatchExact.matches(" " + exampleWord), "Should not match WORD with spaces.");
    Assertions.assertFalse(blacklistEntryMatchExact.matches(exampleWord + " "), "Should not match WORD with spaces.");
    Assertions.assertFalse(blacklistEntryMatchExact.matches(exampleWord + exampleWord), "Should not match double WORD.");
  }

  @Test
  void matchesContainsWordTest() throws Exception {
    Assertions.assertTrue(blacklistEntryContainsWord.matches(exampleWord), "Should match exact MESSAGE.");
    Assertions.assertTrue(blacklistEntryContainsWord.matches(" " + exampleWord), "Should match WORD with spaces.");
    Assertions.assertTrue(blacklistEntryContainsWord.matches(exampleWord + " "), "Should match WORD with spaces.");
    Assertions.assertTrue(blacklistEntryContainsWord.matches(exampleWord + exampleWord), "Should match double WORD.");
    Assertions.assertTrue(blacklistEntryContainsWord.matches("$^*" + exampleWord), "Sound match phase with REGEX control chars.");
    Assertions.assertTrue(blacklistEntryContainsWord.matches(exampleWord.toUpperCase()), "Should match uppercase");
    Assertions.assertFalse(blacklistEntryContainsWord.matches(exampleWord.substring(2)), "Should fail to match missing char");
  }

  @Test
  void matchesAnyTest() throws Exception {
    Assertions.assertTrue(blacklistEntryMatchAll.matches(exampleWord), "Should match exact MESSAGE.");
    Assertions.assertTrue(blacklistEntryMatchAll.matches(" " + exampleWord), "Should match WORD with spaces.");
    Assertions.assertTrue(blacklistEntryMatchAll.matches(exampleWord + " "), "Should match WORD with spaces.");
    Assertions.assertTrue(blacklistEntryMatchAll.matches(exampleWord + exampleWord), "Should match double WORD.");
    Assertions.assertTrue(blacklistEntryMatchAll.matches(exampleWord.toUpperCase()), "Should match uppercase");
    Assertions.assertTrue(blacklistEntryMatchAll.matches(exampleWord.substring(2)), "Should match missing char");
    Assertions.assertTrue(blacklistEntryMatchAll.matches(""), "Should match empty string");
  }
}