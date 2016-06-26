package bot.commands.executors;

import bot.commands.BotCommandException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import twitch.channel.ChannelManager;
import twitch.channel.blacklist.BlacklistEntry;
import twitch.channel.blacklist.BlacklistType;
import twitch.chat.data.OutboundTwitchMessage;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Dominic Hauton on 05/06/2016.
 */
public class BlacklistExecutor implements CommandExecutor {

    private static final ImmutableMap<Character, BlacklistType> blacklistFlagMap = ImmutableMap
            .<Character, BlacklistType>builder()
            .put('w', BlacklistType.word)
            .put('r', BlacklistType.regex)
            .put('m', BlacklistType.message)
            .build();

    private static final BlacklistType defaultBlacklistType = BlacklistType.word;

    BlacklistExecutor() {}

    @Override
    public Collection<OutboundTwitchMessage> executeCommand(
                ImmutableSet<Character> flags,
                ImmutableList<String> args,
                ChannelManager channelManager ) throws BotCommandException {
            boolean removeFromBlacklist = flags.contains('d');
            boolean retrospectiveBan = flags.contains('r');
            boolean forceAction = flags.contains('f');
            if(removeFromBlacklist && retrospectiveBan) {
                throw new BotCommandException("Retrospective bans not available during blacklist removal.");
            }
            BlacklistType blacklistType = args.stream()
                    .filter(blacklistFlagMap::containsKey)
                    .map(blacklistFlagMap::get)
                    .findAny()
                    .orElse(defaultBlacklistType);
            int argCount = args.size();
            if( argCount > 1 ) {
                throw new BotCommandException("There must be one argument. Wrap the phrase in quotes (\") and escape any "
                                              + "inner quotes with backslash (\\)");
            }
            try {
                String blacklistString = args.iterator().next();
                channelManager.blacklistItem(blacklistString, blacklistType, )
            } catch (NoSuchElementException e) {
                throw new BotCommandException("There must be one argument. Escape any starting hyphens with backslash "
                                              + "(\\).");
            }
            return Collections.emptyList();
        }
    }
}
