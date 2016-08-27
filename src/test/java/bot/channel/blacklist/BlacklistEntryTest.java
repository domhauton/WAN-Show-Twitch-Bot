package bot.channel.blacklist;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Pattern;

/**
 * Created by Dominic Hauton on 12/07/2016.
 * <p>
 * Test for a blacklist entry
 */
public class BlacklistEntryTest {
  static final String exampleWord = "foobar";

  BlacklistEntry blacklistEntryMatchExact;
  BlacklistEntry blacklistEntryContainsWord;
  BlacklistEntry blacklistEntryMatchAll;

  @Before
  public void setUp() throws Exception {
    Pattern matchAll = Pattern.compile("^.*$");
    blacklistEntryMatchAll = new BlacklistEntry(matchAll);
    Pattern matchContainsWord = Pattern.compile(
        "^.*" + exampleWord + ".*$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    blacklistEntryContainsWord = new BlacklistEntry(matchContainsWord);
    Pattern matchExact = Pattern.compile("^" + exampleWord + "$");
    blacklistEntryMatchExact = new BlacklistEntry(matchExact);
  }

  @Test
  public void matchesExactTest() throws Exception {
    Assert.assertTrue("Should match exact MESSAGE.", blacklistEntryMatchExact.matches(exampleWord));
    Assert.assertFalse("Should not match WORD with spaces.", blacklistEntryMatchExact.matches(" " + exampleWord));
    Assert.assertFalse("Should not match WORD with spaces.", blacklistEntryMatchExact.matches(exampleWord + " "));
    Assert.assertFalse("Should not match double WORD.", blacklistEntryMatchExact.matches(
        exampleWord + exampleWord));
  }

  @Test
  public void matchesContainsWordTest() throws Exception {
    Assert.assertTrue("Should match exact MESSAGE.", blacklistEntryContainsWord.matches(exampleWord));
    Assert.assertTrue("Should match WORD with spaces.", blacklistEntryContainsWord.matches(" " + exampleWord));
    Assert.assertTrue("Should match WORD with spaces.", blacklistEntryContainsWord.matches(exampleWord + " "));
    Assert.assertTrue("Should match double WORD.", blacklistEntryContainsWord.matches(exampleWord + exampleWord));
    Assert.assertTrue("Sound match phase with REGEX control chars.", blacklistEntryContainsWord.matches(
        "$^*" + exampleWord));
    Assert.assertTrue("Should match uppercase", blacklistEntryContainsWord.matches(exampleWord.toUpperCase()));
    Assert.assertFalse("Should fail to match missing char", blacklistEntryContainsWord.matches(exampleWord.substring(2)));
  }

  @Test
  public void matchesAnyTest() throws Exception {
    Assert.assertTrue("Should match exact MESSAGE.", blacklistEntryMatchAll.matches(exampleWord));
    Assert.assertTrue("Should match WORD with spaces.", blacklistEntryMatchAll.matches(" " + exampleWord));
    Assert.assertTrue("Should match WORD with spaces.", blacklistEntryMatchAll.matches(exampleWord + " "));
    Assert.assertTrue("Should match double WORD.", blacklistEntryMatchAll.matches(exampleWord + exampleWord));
    Assert.assertTrue("Should match uppercase", blacklistEntryMatchAll.matches(exampleWord.toUpperCase()));
    Assert.assertTrue("Should match missing char", blacklistEntryMatchAll.matches(exampleWord.substring(2)));
    Assert.assertTrue("Should match empty string", blacklistEntryMatchAll.matches(""));
  }
}