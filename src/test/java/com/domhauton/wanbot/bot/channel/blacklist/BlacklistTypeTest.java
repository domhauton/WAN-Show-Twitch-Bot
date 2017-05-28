package com.domhauton.wanbot.bot.channel.blacklist;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

/**
 * Created by Dominic Hauton on 12/07/2016.
 * <p>
 * Check enum is functioning correctly.
 */
class BlacklistTypeTest extends BlacklistEntryTest {

  @BeforeEach
  void setup() {
    blacklistEntryMatchAll = new BlacklistEntry(BlacklistType.REGEX.stringToPattern(".*"));
    blacklistEntryContainsWord = new BlacklistEntry(BlacklistType.WORD.stringToPattern(exampleWord));
    blacklistEntryMatchExact = new BlacklistEntry(BlacklistType.MESSAGE.stringToPattern(exampleWord));
  }

  @Test
  void regexBlackListTypeConverterTest() throws Exception {
    Pattern actualPattern = BlacklistType.REGEX.stringToPattern(".*");
    Pattern expectedPattern = Pattern.compile(".*");
    Assertions.assertEquals(expectedPattern.pattern(), actualPattern.pattern(), "Check produced patterns are the same");
  }

  @Test
  void wordBlackListTypeConverterTest() throws Exception {
    String word = "foobar";
    Pattern actualPattern = BlacklistType.WORD.stringToPattern(word);
    Pattern expectedPattern = Pattern.compile("^.*\\Q" + word + "\\E.*$");
    Assertions.assertEquals(expectedPattern.pattern(), actualPattern.pattern(),
        "Check produced patterns are the same. If failing check manual escape.");
  }

  @Test
  void wordBlackListTypeConverterTestWithEscapes() throws Exception {
    String word = "foobar\\E";
    Pattern actualPattern = BlacklistType.WORD.stringToPattern(word);
    String quotedWord = Pattern.quote(word);
    Pattern expectedPattern = Pattern.compile("^.*" + quotedWord + ".*$");
    Assertions.assertEquals(expectedPattern.pattern(), actualPattern.pattern(),
        "Check produced patterns are the same.");
  }

  @Test
  void messageBlackListTypeConverterTest() throws Exception {
    String message = "foobar";
    Pattern actualPattern = BlacklistType.MESSAGE.stringToPattern(message);
    Pattern expectedPattern = Pattern.compile("^\\Q" + message + "\\E$");
    Assertions.assertEquals(expectedPattern.pattern(), actualPattern.pattern(),
        "Check produced patterns are the same. If failing check escapes.");
  }

  @Test
  void messageBlackListTypeConverterTestWithEscapes() throws Exception {
    String message = "foobar\\E";
    Pattern actualPattern = BlacklistType.MESSAGE.stringToPattern(message);
    String quotedWord = Pattern.quote(message);
    Pattern expectedPattern = Pattern.compile("^" + quotedWord + "$", Pattern.MULTILINE);
    Assertions.assertEquals(expectedPattern.pattern(), actualPattern.pattern(),
        "Check produced patterns are the same.");
  }
}