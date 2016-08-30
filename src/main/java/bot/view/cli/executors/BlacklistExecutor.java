package bot.view.cli.executors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import bot.channel.ChannelManager;
import bot.channel.ChannelOperationException;
import bot.channel.blacklist.BlacklistEntry;
import bot.channel.blacklist.BlacklistType;
import bot.channel.message.TwitchMessage;
import bot.channel.timeouts.TimeoutReason;
import bot.view.cli.BotCommandException;
import twitch.chat.data.OutboundTwitchMessage;
import twitch.chat.data.OutboundTwitchTimeout;
import twitch.chat.data.OutboundTwitchWhisper;

/**
 * Created by Dominic Hauton on 05/06/2016.
 * <p>
 * Handles blacklist command executions.
 */
public class BlacklistExecutor implements CommandExecutor {

  private static final ImmutableMap<Character, BlacklistType> blacklistFlagMap = ImmutableMap
      .<Character, BlacklistType>builder()
      .put('w', BlacklistType.WORD)
      .put('r', BlacklistType.REGEX)
      .put('m', BlacklistType.MESSAGE)
      .build();

  private static final BlacklistType defaultBlacklistType = BlacklistType.WORD;

  @Override
  public BotCommandResult executeCommand(
      ImmutableSet<Character> flags,
      ImmutableList<String> args,
      ChannelManager channelManager) throws BotCommandException {
    boolean removeFromBlacklist = flags.contains('d');
    boolean ignorePreviousOffenses = flags.contains('i');

    BlacklistType blacklistType = extractBlackListType(flags);
    String blackListItem = extractBlackListItem(args);

    if (removeFromBlacklist) {
      return flags.contains('f')
          ? removeBlacklistItem(blackListItem, channelManager)
          : removeBlacklistItem(channelManager, blackListItem, blacklistType);
    } else {
      return addBlacklistItem(channelManager,
          ignorePreviousOffenses,
          blacklistType,
          blackListItem);
    }

  }

  private BotCommandResult addBlacklistItem(ChannelManager channelManager,
                                            boolean ignorePreviousOffenses,
                                            BlacklistType blacklistType,
                                            String blackListItem) throws BotCommandException {
    Collection<TwitchMessage> retrospectiveBreaches;
    try {
      retrospectiveBreaches = ignorePreviousOffenses
          ? channelManager.blacklistItem(blackListItem, blacklistType, 0)
          : channelManager.blacklistItem(blackListItem, blacklistType);
    } catch (ChannelOperationException e) {
      throw new BotCommandException("There was a problem executing your command: "
          + e.getMessage());
    }

    Collection<OutboundTwitchMessage> outboundTwitchMessages = retrospectiveBreaches.stream()
        .map(TwitchMessage::getUsername)
        .map(username -> Arrays.asList(
            new OutboundTwitchTimeout(
                channelManager.getChannelName(),
                username,
                channelManager.addUserTimeout(username, TimeoutReason.BLACKLISTED_WORD)
            ),
            new OutboundTwitchWhisper(TimeoutReason.BLACKLISTED_WORD.getMessage(), username)
        ))
        .collect(LinkedList::new, LinkedList::addAll, LinkedList::addAll);

    OutboundTwitchMessage publicAdditionSuccessMessage = new OutboundTwitchMessage(
        "Added new item to blacklist. " + retrospectiveBreaches.size()
            + " retrospective timeouts issued.", channelManager.getChannelName());
    outboundTwitchMessages.add(publicAdditionSuccessMessage);
    return new BotCommandResult(outboundTwitchMessages,
        "Successfully added: " + blacklistType + " " + blackListItem);
  }

  /**
   * Will attempt to remove the given blacklisted word with fuzzy inexact matching.
   *
   * @param blacklistPhrase String to search the current blacklist for.
   */
  private BotCommandResult removeBlacklistItem(String blacklistPhrase,
                                               ChannelManager channelManager) {
    Collection<BlacklistEntry> removedBlacklistEntries
        = channelManager.removeBlacklistItem(blacklistPhrase);
    if (removedBlacklistEntries.isEmpty()) {
      return new BotCommandResult(Collections.emptyList(),
          "Could not find any entry matching " + blacklistPhrase);
    } else {
      return new BotCommandResult(Collections.emptyList(),
          "Found and removed: " + removedBlacklistEntries
              .stream()
              .map(BlacklistEntry::toString)
              .collect(Collectors.joining(", ")));
    }
  }

  /**
   * Removes the exact given phrase with the given type.
   */
  private BotCommandResult removeBlacklistItem(ChannelManager channelManager,
                                               String blacklistPhrase,
                                               BlacklistType blacklistType) {
    try {
      channelManager.removeBlacklistItem(blacklistPhrase, blacklistType);
      return new BotCommandResult(Collections.emptyList(),
          "Successfully removed " + blacklistPhrase + " from blacklist");
    } catch (ChannelOperationException e) {
      return new BotCommandResult(Collections.emptyList(),
          "Blacklist item " + blacklistPhrase + " not found. Try with flag -f.");
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
  static BlacklistType extractBlackListType(Collection<Character> flags) {
    return flags.stream()
        .filter(blacklistFlagMap::containsKey)
        .map(blacklistFlagMap::get)
        .findFirst()
        .orElse(defaultBlacklistType);
  }
}
