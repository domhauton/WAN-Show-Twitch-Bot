package twitch.channel.blacklist;

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

    BlacklistEntry m_blacklistEntryMatchExact;
    BlacklistEntry m_blacklistEntryContainsWord;
    BlacklistEntry m_blacklistEntryMatchAll;

    @Before
    public void setUp() throws Exception {
        Pattern matchAll = Pattern.compile("^.*$");
        m_blacklistEntryMatchAll = new BlacklistEntry(matchAll);
        Pattern matchContainsWord = Pattern.compile(
                "^.*" + exampleWord + ".*$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        m_blacklistEntryContainsWord = new BlacklistEntry(matchContainsWord);
        Pattern matchExact = Pattern.compile("^" + exampleWord + "$");
        m_blacklistEntryMatchExact = new BlacklistEntry(matchExact);
    }

    @Test
    public void matchesExactTest() throws Exception {
        Assert.assertTrue("Should match exact MESSAGE.", m_blacklistEntryMatchExact.matches(exampleWord));
        Assert.assertFalse("Should not match WORD with spaces.", m_blacklistEntryMatchExact.matches(" " + exampleWord));
        Assert.assertFalse("Should not match WORD with spaces.", m_blacklistEntryMatchExact.matches(exampleWord + " "));
        Assert.assertFalse("Should not match double WORD.", m_blacklistEntryMatchExact.matches(
                exampleWord + exampleWord));
    }

    @Test
    public void matchesContainsWordTest() throws Exception {
        Assert.assertTrue("Should match exact MESSAGE.", m_blacklistEntryContainsWord.matches(exampleWord));
        Assert.assertTrue("Should match WORD with spaces.", m_blacklistEntryContainsWord.matches(" " + exampleWord));
        Assert.assertTrue("Should match WORD with spaces.", m_blacklistEntryContainsWord.matches(exampleWord + " "));
        Assert.assertTrue("Should match double WORD.", m_blacklistEntryContainsWord.matches(exampleWord + exampleWord));
        Assert.assertTrue("Sound match phase with REGEX control chars.", m_blacklistEntryContainsWord.matches(
                "$^*" + exampleWord));
        Assert.assertTrue("Should match uppercase", m_blacklistEntryContainsWord.matches(exampleWord.toUpperCase()));
        Assert.assertFalse("Should fail to match missing char", m_blacklistEntryContainsWord.matches(exampleWord.substring(2)));
    }

    @Test
    public void matchesAnyTest() throws Exception {
        Assert.assertTrue("Should match exact MESSAGE.", m_blacklistEntryMatchAll.matches(exampleWord));
        Assert.assertTrue("Should match WORD with spaces.", m_blacklistEntryMatchAll.matches(" " + exampleWord));
        Assert.assertTrue("Should match WORD with spaces.", m_blacklistEntryMatchAll.matches(exampleWord + " "));
        Assert.assertTrue("Should match double WORD.", m_blacklistEntryMatchAll.matches(exampleWord + exampleWord));
        Assert.assertTrue("Should match uppercase", m_blacklistEntryMatchAll.matches(exampleWord.toUpperCase()));
        Assert.assertTrue("Should match missing char", m_blacklistEntryMatchAll.matches(exampleWord.substring(2)));
        Assert.assertTrue("Should match empty string", m_blacklistEntryMatchAll.matches(""));
    }
}