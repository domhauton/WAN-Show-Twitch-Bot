package com.domhauton.wanbot.bot.channel.blacklist;

import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Created by Dominic Hauton on 23/05/2016.
 *
 * Converts desired blacklist types to REGEX.
 */
public enum BlacklistType {
  WORD(word -> "^.*" + Pattern.quote(word) + ".*$"),
  MESSAGE(message -> "^" + Pattern.quote(message) + "$"),
  REGEX(regex -> regex);

  private Function<String, String> regexConverter;

  BlacklistType(Function<String, String> regexConverter) {
    this.regexConverter = regexConverter;
  }

  public Pattern stringToPattern(String input) {
    String convertedInput = regexConverter.apply(input);
    return Pattern.compile(convertedInput, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
  }
}
