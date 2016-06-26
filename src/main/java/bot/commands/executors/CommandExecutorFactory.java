package bot.commands.executors;

import bot.commands.BotCommandException;
import bot.commands.TwitchCommand;

/**
 * Created by Dominic Hauton on 04/06/2016.
 */
public abstract class CommandExecutorFactory {
    public static CommandExecutor getExecutor(TwitchCommand twitchCommand) throws BotCommandException {
        switch (twitchCommand) {
            case blacklist:
                return new BlacklistExecutor();
            default:
                throw new BotCommandException("No implementation for command: " + twitchCommand.toString());
        }
    }
}
