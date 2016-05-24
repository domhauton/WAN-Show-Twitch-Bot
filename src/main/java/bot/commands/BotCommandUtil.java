package bot.commands;

import twitch.chat.data.OutboundTwitchMessage;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by Dominic Hauton on 23/05/2016.
 *
 * Contains BotCommand functions
 */
public final class BotCommandUtil {

    public static Collection<OutboundTwitchMessage> blackList(BotCommandMessage botCommandMessage) throws BotCommandException{
        boolean removeFromBlacklist = botCommandMessage.containsFlag('d');
        boolean retrospectiveBan = botCommandMessage.containsFlag('r');
        boolean forceAction = botCommandMessage.containsFlag('f');
        if(removeFromBlacklist && retrospectiveBan) {
            throw new BotCommandException("Retrospective bans not available during blacklist removal.");
        }
        long typeFlagCount = "wmr".chars()
                .mapToObj(value -> (char) value)
                .filter(botCommandMessage::containsFlag)
                .count();
        if (typeFlagCount > 1) {
            throw new BotCommandException("You have specified too many type flags. Choose one.");
        }
        int argCount = botCommandMessage.getArgs().size();
        if( argCount > 1 ) {
            throw new BotCommandException("There must be one argument. Wrap the phrase in quotes (\") and escape any "
                                          + "inner quotes with backslash (\\)");
        }
        if( argCount == 0 ) {
            throw new BotCommandException("There must be one argument. Escape any starting hyphens with backslash "
                                          + "(\\).");
        }
        // FIXME Finish blacklist command
        return Collections.emptyList();
    }
}
