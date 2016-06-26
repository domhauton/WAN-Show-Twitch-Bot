package bot.commands.response;

import com.google.common.collect.ImmutableList;

/**
 * Created by Dominic Hauton on 04/06/2016.
 *
 * Immutable commandResponse for any incoming command
 */
public class CommandResponse {
    private final ImmutableList<String> publicMessages;
    private final ImmutableList<PrivateResponse> userMessages;
    private final String commandResultMessage;
    private final CommandSuccess commandSuccess;

    CommandResponse(ImmutableList<String> publicMessages, ImmutableList<PrivateResponse> userMessages, String commandResultMessage, CommandSuccess commandSuccess) {
        this.publicMessages = publicMessages;
        this.userMessages = userMessages;
        this.commandResultMessage = commandResultMessage;
        this.commandSuccess = commandSuccess;
    }

    public ImmutableList<String> getPublicMessages() {
        return publicMessages;
    }

    public ImmutableList<PrivateResponse> getUserMessages() {
        return userMessages;
    }

    public String getCommandResultMessage() {
        return commandResultMessage;
    }

    public CommandSuccess getCommandSuccess() {
        return commandSuccess;
    }
}
