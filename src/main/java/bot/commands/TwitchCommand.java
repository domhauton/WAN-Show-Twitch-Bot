package bot.commands;

import java.util.*;
import java.util.stream.Stream;

/**
 * Created by Dominic Hauton on 23/05/2016.
 *
 *
 */
public enum TwitchCommand {
    blacklist("blacklist", "bl");

    private Collection<String> aliases;

    TwitchCommand(String... aliases) {
        this.aliases = new HashSet<>(Arrays.asList(aliases));
    }

    public static TwitchCommand getCommand(String commandName) throws BotCommandException {
        return Stream.of(TwitchCommand.values())
                .filter(x -> x.aliasMatches(commandName))
                .findAny()
                .orElseThrow(() -> new BotCommandException("Could not find matching command for: " + commandName));
    }

    public boolean aliasMatches(String alias) {
        return aliases.contains(alias);
    }
}
