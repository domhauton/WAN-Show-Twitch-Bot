package bot.commands.executors;

import bot.commands.BotCommand;
import bot.commands.BotCommandException;
import twitch.channel.ChannelManager;
import twitch.chat.data.OutboundTwitchMessage;

import java.util.Collection;


/**
 * Created by Dominic Hauton on 23/05/2016.
 * <p>
 * Contains BotCommand functions
 */
public final class BotCommandExecutor {

    private CommandExecutorFactory commandExecutorFactory;

    public BotCommandExecutor() {
        commandExecutorFactory = new CommandExecutorFactory();
    }

    public Collection<OutboundTwitchMessage> parseCommand(BotCommand botCommand, ChannelManager channelManager) throws BotCommandException {
        CommandExecutor commandExecutor = commandExecutorFactory.getExecutor(botCommand.getTwitchCommand());
        return commandExecutor.executeCommand(botCommand.getFlags(), botCommand.getArgs(), channelManager);
    }
}
