package com.domhauton.wanbot.bot.view.cli;

import com.domhauton.wanbot.bot.channel.TwitchUser;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Created by Dominic Hauton on 06/05/2016.
 *
 * Data class for a processed com.domhauton.wanbot.bot command.
 */
public class BotCommand {
  private final static char FLAG_PREFIX = '-';

  private final TwitchUser twitchUser;
  private final BotCommandType botCommandType;
  private final ImmutableSet<Character> flags;
  private final ImmutableList<String> args;

  public BotCommand(TwitchUser twitchUser, List<String> commandMessage) {
    this.twitchUser = twitchUser;
    if (commandMessage.isEmpty()) {
      botCommandType = BotCommandType.UNKNOWN;
      flags = ImmutableSet.of();
      args = ImmutableList.of();
    } else {
      String commandName = commandMessage.get(0);
      botCommandType = BotCommandType.getCommand(commandName);
      flags = commandMessage.stream()
          .filter(command -> command.startsWith(String.valueOf(FLAG_PREFIX)))
          .map(flags -> flags.substring(1))
          .map(String::toLowerCase)
          .map(CharSequence::chars)
          .flatMap(intStream -> intStream.mapToObj(i -> (char) i))
          .collect(Collectors.collectingAndThen(Collectors.toSet(), ImmutableSet::copyOf));
      args = commandMessage.stream()
          .filter(command -> !command.startsWith(String.valueOf(FLAG_PREFIX)))
          .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
    }
  }

  BotCommand(TwitchUser twitchUser, BotCommandType botCommandType, ImmutableSet<Character> flags, ImmutableList<String> args) {
    this.twitchUser = twitchUser;
    this.botCommandType = botCommandType;
    this.flags = flags;
    this.args = args;
  }

  public TwitchUser getTwitchUser() {
    return twitchUser;
  }

  public BotCommandType getBotCommandType() {
    return botCommandType;
  }

  public ImmutableSet<Character> getFlags() {
    return flags;
  }

  public ImmutableList<String> getArgs() {
    return args;
  }
}
