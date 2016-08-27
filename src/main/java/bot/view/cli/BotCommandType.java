package bot.view.cli;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Stream;

import bot.channel.permissions.UserPermission;
import bot.view.cli.executors.BlacklistExecutor;
import bot.view.cli.executors.ChannelSettingModifier;
import bot.view.cli.executors.CommandExecutor;
import bot.view.cli.executors.PermissionModifier;
import bot.view.cli.executors.UnknownCommandExecutor;

/**
 * Created by Dominic Hauton on 23/05/2016.
 */
enum BotCommandType {
  BLACKLIST(new BlacklistExecutor(), UserPermission.BotModerator, new String[]{"blacklist", "bl"}),
  PERMISSIONS(new PermissionModifier(), UserPermission.BotAdmin, new String[]{"perm", "permission", "chmod"}),
  SHOW(new ChannelSettingModifier(), UserPermission.BotModerator, new String[]{"channel", "show"}),
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
