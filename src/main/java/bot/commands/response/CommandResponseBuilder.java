package bot.commands.response;

import com.google.common.collect.ImmutableList;

import java.util.LinkedList;

/**
 * Created by Dominic Hauton on 04/06/2016.
 */
public class CommandResponseBuilder {
    private final LinkedList<String> publicMessages;
    private final LinkedList<PrivateResponse> userMessages;
    private final String commandResultMessage;

    public CommandResponseBuilder() {
        publicMessages = new LinkedList<>();
        userMessages = new LinkedList<>();
        commandResultMessage = "";
    }

    public CommandResponse build(CommandSuccess commandSuccess) {
        return new CommandResponse(
                ImmutableList.copyOf(publicMessages),
                ImmutableList.copyOf(userMessages),
                commandResultMessage,
                commandSuccess);
    }
}
