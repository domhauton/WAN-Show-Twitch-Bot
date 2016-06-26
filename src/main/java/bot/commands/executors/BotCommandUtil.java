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
                blacklist(flags, args, channelManager);
            default:
                throw new BotCommandException("No implementation for command: " + twitchCommand.toString());
        }
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
