package channel.message;

/**
 * Created by Dominic Hauton on 12/03/2016.
 */
public class MessageManager {
    private CommandManager commandManager;
    private MessageBuffer messageBuffer;

    public MessageManager() {
        commandManager = new CommandManager();
        messageBuffer = new MessageBuffer(100);
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public MessageBuffer getMessageBuffer() {
        return messageBuffer;
    }
}
