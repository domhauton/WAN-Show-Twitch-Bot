package com.domhauton.wanbot.bot.view.cli;

import com.domhauton.wanbot.bot.channel.permissions.UserPermission;
import com.domhauton.wanbot.bot.view.cli.executors.BlacklistModifier;
import com.domhauton.wanbot.bot.view.cli.executors.CommandExecutor;
import com.domhauton.wanbot.bot.view.cli.executors.LinkRepeater;
import com.domhauton.wanbot.bot.view.cli.executors.UnknownCommandExecutor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Stream;

/**
 * Created by Dominic Hauton on 23/05/2016.
 */
enum BotCommandType {
  BLACKLIST(new BlacklistModifier(), UserPermission.BotModerator, new String[]{"blacklist", "bl"}),
  LINK_REPEATER(new LinkRepeater(), UserPermission.BotAdmin, new String[]{"repeat", "rep", "r"}),
  UNKNOWN(new UnknownCommandExecutor(), UserPermission.ChannelModerator, new String[]{""});

  private Collection<String> aliases;
  private CommandExecutor commandExecutor;
  private UserPermission userPermission;

  BotCommandType(CommandExecutor commandExecutor, UserPermission userPermission, String[] aliases) {
    this.commandExecutor = commandExecutor;
    this.userPermission = userPermission;
    this.aliases = new HashSet<>(Arrays.asList(aliases));
  }

  static BotCommandType getCommand(String commandName) {
    return Stream.of(BotCommandType.values())
        .filter(x -> x.aliasMatches(commandName))
        .findAny()
        .orElse(UNKNOWN);
  }

  boolean aliasMatches(String alias) {
    return aliases.contains(alias);
  }

  CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }

  UserPermission requiredUserPermissionLevel() {
    return userPermission;
  }
}
