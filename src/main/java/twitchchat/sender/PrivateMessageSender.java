package twitchchat.sender;

import com.google.inject.name.Named;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import twitchchat.util.AsyncEventBuffer;
import twitchchat.util.OutboundMessage;
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

    private final static Period messageDelay = new Period(400, PeriodType.millis());

    @Inject
    public PrivateMessageSender(
            @Named("twitch.username") String twitchUsername,
            @Named("twitch.oauth.token") String oAuthToken,
            @Named("twitch.irc.whisper.channel") String whisperChannelName,
            @Named("twitch.irc.whisper.server") String twitchGroupServerName,
            @Named("twitch.irc.whisper.port") Integer twitchGroupServerPort,
            @Named("twitch.irc.whisper.eventCountPerWindow") Integer maxEventCountPerWindow,
            @Named("twitch.irc.whisper.eventCountWindowSize") Integer windowSizeSeconds) {
        super(twitchUsername, oAuthToken, new AsyncEventBuffer(maxEventCountPerWindow, windowSizeSeconds));

    }

    @Override
    connect(String twitchChannelName, String ircServer, Integer ircPort) {
        super(twitchChannelName, ircServer, ircPort);
        log.debug("Created PrivateMessageSender");
        whisperChannel = twitchChannelName;
        whisperHandshake();

    }

    private void whisperHandshake() {
        String handshake = "CAP REQ :twitch.tv/commands";
        twitchChatConnector.sendRawMessage(handshake);
        log.debug("Sent handshake message: {}", handshake);
        twitchChatConnector.setMessageDelay(messageDelay);
    }

    public void sendWhisperAsync(OutboundMessage outboundMessage){
        String whisperPayload = ".w " + outboundMessage.getTarget() + " " + outboundMessage.getPayload();
        OutboundMessage formattedWhisperMessage = outboundMessage.setPayload(whisperPayload).setTarget(whisperChannel);
        sendMessageAsync(formattedWhisperMessage);
    }
}
