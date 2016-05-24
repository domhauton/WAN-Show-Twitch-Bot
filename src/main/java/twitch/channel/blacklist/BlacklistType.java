package twitch.channel.blacklist;

import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Created by Dominic Hauton on 23/05/2016.
 */
public enum BlacklistType {
    word( word -> "*" + Pattern.quote(word) + "*"),
    message( message -> "^" + Pattern.quote(message) + "$"),
    regex( regex -> regex );

    private Function<String, String> regexConverter;

    BlacklistType(Function<String, String> regexConverter) {
        this.regexConverter = regexConverter;
    }

    public Pattern stringToPattern(String input) {
        String convertedInput = regexConverter.apply(input);
        return Pattern.compile(convertedInput, Pattern.CASE_INSENSITIVE);
    }
}
