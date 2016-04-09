package channel.commands;

import channel.data.TwitchMessage;

/**
 * Created by Dominic Hauton on 28/03/2016.
 */
public class CommandResponse {
    private String message;
    private TwitchMessage originalMessage;
    private boolean sendToChannel = false;
    private boolean whisperToUser = false;

    public CommandResponse(TwitchMessage originalMessage, String message) {
        this.originalMessage = originalMessage;
        this.message = message;
    }

    public CommandResponse setWhisperToUser(boolean whisperToUser) {
        this.whisperToUser = whisperToUser;
        return this;
    }

    public CommandResponse setSendToChannel(boolean sendToChannel) {
        this.sendToChannel = sendToChannel;
        return this;
    }
}
