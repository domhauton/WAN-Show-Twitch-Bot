package bot.commands.executors;

import bot.commands.BotCommandException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.joda.time.Duration;
import twitch.channel.ChannelManager;
import twitch.channel.ChannelOperationException;
import twitch.channel.blacklist.BlacklistType;
import twitch.channel.message.TwitchMessage;
import twitch.chat.data.OutboundTwitchMessage;
import twitch.chat.data.OutboundTwitchTimeout;
import twitch.chat.data.OutboundTwitchWhisper;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Dominic Hauton on 05/06/2016.
 * <p>
 * Handles blacklist command executions.
 */
public class BlacklistExecutor implements CommandExecutor {

    private static final ImmutableMap<Character, BlacklistType> blacklistFlagMap = ImmutableMap.<Character, BlacklistType>builder()
            .put('w', BlacklistType.WORD)
            .put('r', BlacklistType.REGEX)
            .put('m', BlacklistType.MESSAGE)
            .build();

    private static final BlacklistType defaultBlacklistType = BlacklistType.WORD;

    @Override
    public Collection<OutboundTwitchMessage> executeCommand(ImmutableSet<Character> flags, ImmutableList<String> args, ChannelManager channelManager) throws BotCommandException {
        boolean removeFromBlacklist = flags.contains('d');
        boolean ignorePreviousOffenses = flags.contains('i');

        BlacklistType blacklistType = extractBlackListType(flags);
        String blackListItem = extractBlackListItem(args);
        try {
            if (removeFromBlacklist) {
                throw new BotCommandException("Blacklist removal not yet implemented.");
                //TODO Implement removal
            } else {
                Collection<TwitchMessage> retrospectivelyBreaches = ignorePreviousOffenses
                                                                    ? channelManager.blacklistItem(blackListItem, blacklistType, 0)
                                                                    : channelManager.blacklistItem(blackListItem, blacklistType);

                OutboundTwitchMessage publicAdditionSuccessMessage = new OutboundTwitchMessage(
                        "Added new item to blacklist. " + retrospectivelyBreaches.size()
                        + " retrospective timeouts issued.", channelManager.getChannelName());

                Collection<OutboundTwitchMessage> outboundMessages = convertRetrospectiveBreachesToBans(retrospectivelyBreaches);
                outboundMessages.add(publicAdditionSuccessMessage);
                return outboundMessages;
            }
        } catch (ChannelOperationException e) {
            throw new BotCommandException("There was a problem executing your command: " + e.getMessage());
        }
    }

    /**
     * Takes the only item in the set.
     *
     * @throws BotCommandException If incorrect amount of args.
     */
    private String extractBlackListItem(List<String> args) throws BotCommandException {
        Supplier<BotCommandException> botCommandException = () -> new BotCommandException(
                "There must be exactly one " + "argument. Wrap the phrase in quotes (\") and "
                + "escape any inner quotes with " + "backslash (\\)");
        String firstArgument = args.stream().findFirst().orElseThrow(botCommandException);
        if (args.size() > 1) {
            throw botCommandException.get();
        }
        return firstArgument;
    }

    /**
     * Finds the type from one of the type flags in the passed argument.
     */
    private BlacklistType extractBlackListType(Set<Character> flags) {
        return flags.stream()
                .filter(blacklistFlagMap::containsKey)
                .map(blacklistFlagMap::get)
                .findAny()
                .orElse(defaultBlacklistType);
    }

    private Collection<OutboundTwitchMessage> convertRetrospectiveBreachesToBans(Collection<TwitchMessage> retrospectiveBreaches) {
        return retrospectiveBreaches.stream()
                .map(twitchMessage -> Stream.of(createRetrospectiveTimeoutCommand(twitchMessage.getUsername(), twitchMessage
                        .getTwitchChannel()), createRetrospectiveTimeoutNotification(twitchMessage.getUsername())))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    private OutboundTwitchWhisper createRetrospectiveTimeoutNotification(String twitchUsername) {
        return new OutboundTwitchWhisper("One of your recent messages now breaches the channel blacklist. You have "
                                         + "been given a retrospective timeout. Please contact mods if you have "
                                         + "any further questions.", twitchUsername);
    }

    private OutboundTwitchTimeout createRetrospectiveTimeoutCommand(String twitchUsername, String targetChannel) {
        //TODO use channelSpecificTimeoutValue. Pass in channel and use DAO. Needs to submit time to TOM
        return new OutboundTwitchTimeout(targetChannel, twitchUsername, Duration.standardSeconds(45));
    }
}
