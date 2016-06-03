package bot.commands.executors;

import bot.commands.BotCommandException;
import bot.commands.TwitchCommand;
import twitch.channel.ChannelManager;
import twitch.chat.data.OutboundTwitchMessage;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * Created by Dominic Hauton on 23/05/2016.
 *
 * Contains BotCommand functions
 */
public final class BotCommandUtil {

    static Collection<OutboundTwitchMessage> parseCommand(TwitchCommand twitchCommand,
                                                          Set<Character> flags,
                                                          List<String> args,
                                                          ChannelManager channelManager) throws BotCommandException {
        switch (twitchCommand) {
            case blacklist:
                blacklist(flags, args, channelManager)
            default:
                throw new BotCommandException("No implementation for command: " + twitchCommand.toString());
        }
    }

    static Collection<OutboundTwitchMessage> blackList(Set<Character> flags,
                                                       List<String> args,
                                                       ChannelManager channelManager) throws BotCommandException{
        boolean removeFromBlacklist = flags.contains('d');
        boolean retrospectiveBan = flags.contains('r');
        boolean forceAction = flags.contains('f');
        if(removeFromBlacklist && retrospectiveBan) {
            throw new BotCommandException("Retrospective bans not available during blacklist removal.");
        }
        long typeFlagCount = "wmr".chars()
                .mapToObj(value -> (char) value)
                .filter(flags::contains)
                .count();
        if (typeFlagCount > 1) {
            throw new BotCommandException("You have specified too many type flags. Choose one.");
        }
        int argCount = args.size();
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

    /**
     * Counts the given flags within the given flags. Returns true or false based on the maxCount allowed.
     * @param relevantFlags Flags that should be counted
     * @param flagsToCount
     * @param maxCount
     * @return
     */
    static boolean verifyFlagCount(Set<Character> relevantFlags, Set<Character> flagsToCount, long maxCount) {

    }
}
