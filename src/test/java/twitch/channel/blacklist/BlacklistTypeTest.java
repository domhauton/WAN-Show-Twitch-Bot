package twitch.channel.blacklist;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Pattern;

/**
 * Created by Dominic Hauton on 12/07/2016.
 *
 * Check enum is functioning correctly.
 */
public class BlacklistTypeTest extends BlacklistEntryTest{

    @Before
    public void setup(){
        m_blacklistEntryMatchAll = new BlacklistEntry(BlacklistType.REGEX.stringToPattern(".*"));
        m_blacklistEntryContainsWord = new BlacklistEntry(BlacklistType.WORD.stringToPattern(exampleWord));
        m_blacklistEntryMatchExact = new BlacklistEntry(BlacklistType.MESSAGE.stringToPattern(exampleWord));
    }

    @Test
    public void regexBlackListTypeConverterTest() throws Exception {
        Pattern actualPattern = BlacklistType.REGEX.stringToPattern(".*");
        Pattern expectedPattern = Pattern.compile(".*");
        Assert.assertEquals("Check produced patterns are the same", expectedPattern.pattern(), actualPattern.pattern());
    }

    @Test
    public void wordBlackListTypeConverterTest() throws Exception {
        String word = "foobar";
        Pattern actualPattern = BlacklistType.WORD.stringToPattern(word);
        Pattern expectedPattern = Pattern.compile("^.*\\Q" + word + "\\E.*$");
        Assert.assertEquals("Check produced patterns are the same. If failing check manual escape.", expectedPattern
                .pattern(),
                actualPattern.pattern());
    }

    @Test
    public void wordBlackListTypeConverterTestWithEscapes() throws Exception {
        String word = "foobar\\E";
        Pattern actualPattern = BlacklistType.WORD.stringToPattern(word);
        String quotedWord = Pattern.quote(word);
        Pattern expectedPattern = Pattern.compile("^.*" + quotedWord + ".*$");
        Assert.assertEquals("Check produced patterns are the same.", expectedPattern.pattern(), actualPattern.pattern());
    }

    @Test
    public void messageBlackListTypeConverterTest() throws Exception {
        String message = "foobar";
        Pattern actualPattern = BlacklistType.MESSAGE.stringToPattern(message);
        Pattern expectedPattern = Pattern.compile("^\\Q" + message + "\\E$");
        Assert.assertEquals("Check produced patterns are the same. If failing check escapes.", expectedPattern.pattern
                (), actualPattern.pattern());
    }

    @Test
    public void messageBlackListTypeConverterTestWithEscapes() throws Exception {
        String message = "foobar\\E";
        Pattern actualPattern = BlacklistType.MESSAGE.stringToPattern(message);
        String quotedWord = Pattern.quote(message);
        Pattern expectedPattern = Pattern.compile("^" + quotedWord + "$", Pattern.MULTILINE);
        Assert.assertEquals("Check produced patterns are the same.", expectedPattern.pattern(), actualPattern.pattern());
    }
}