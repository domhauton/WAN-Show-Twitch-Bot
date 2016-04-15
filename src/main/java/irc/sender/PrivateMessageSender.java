package irc.sender;

import com.google.inject.name.Named;
import irc.util.AsyncEventBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;

/**
 * Created by Dominic on 08/08/2015.
 *
 * Used to send whispers to other users.
 */
public class PrivateMessageSender extends IRCConnection {

    private final Logger log = LogManager.getLogger();
    private String whisperChannel;

    /**
     * @return return false if message could not be sent due to full queue
     */
    public boolean trySendWhisper(String recipient, String payload){
        String formattedMessage = String.format(".w %s %s", recipient, payload);
        return trySendMessage(whisperChannel, formattedMessage);
    }

    public void sendWhisperAsync(String recipient, String payload){
        String formattedMessage = String.format(".w %s %s", recipient, payload);
        sendMessageAsync(whisperChannel, formattedMessage);
    }

    @Inject
    public PrivateMessageSender(
            @Named("twitch.username") String twitchUsername,
            @Named("twitch.oauth.token") String oAuthToken,
            @Named("twitch.irc.whisper.twitchChannel") String twitchChannelName,
            @Named("twitch.irc.whisper.server") String ircServer,
            @Named("twitch.irc.whisper.port") Integer ircPort,
            @Named("twitch.irc.whisper.eventCountPerWindow") Integer maxEventCountPerWindow,
            @Named("twitch.irc.whisper.eventCountWindowSize") Integer windowSizeSeconds) {
        super(twitchUsername, oAuthToken, new AsyncEventBuffer(maxEventCountPerWindow, windowSizeSeconds));
        connect(twitchChannelName, ircServer, ircPort);
        log.debug("Created PrivateMessageSender");
        whisperChannel = twitchChannelName;
        whisperHandshake();
    }

    private void whisperHandshake() {
        String handshake = "CAP REQ :twitch.tv/commands";
        sendRawLine(handshake);
        log.debug("Sent handshake message: {}", handshake);
        setMessageDelay(400L);
    }
}
