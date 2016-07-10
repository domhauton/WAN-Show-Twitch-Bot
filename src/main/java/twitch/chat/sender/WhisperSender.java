package twitch.chat.sender;

import com.google.inject.name.Named;
import irc.ChatHandshake;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import twitch.chat.data.OutboundTwitchMessage;
import twitch.chat.exceptions.TwitchChatException;

import javax.inject.Inject;

/**
 * Created by Dominic on 08/08/2015.
 *
 * Used to send whispers to other users.
 */
class WhisperSender extends MessageSender {

    private String whisperChannelName;
    private String twitchGroupServerName;
    private Integer twitchGroupServerPort;

    private final static Period messageDelay = new Period(400, PeriodType.millis());

    @Inject
    public WhisperSender(
            @Named("twitch.username") String twitchUsername,
            @Named("twitch.oauth.token") String oAuthToken,
            @Named("twitch.irc.whisper.twitchChannel") String whisperChannelName,
            @Named("twitch.irc.whisper.server") String twitchGroupServerName,
            @Named("twitch.irc.whisper.port") Integer twitchGroupServerPort,
            @Named("twitch.irc.whisper.eventCountPerWindow") Integer maxEventCountPerWindow,
            @Named("twitch.irc.whisper.eventCountWindowSize") Integer windowSizeSeconds) {
        super(twitchUsername, oAuthToken, new AsyncEventBuffer(maxEventCountPerWindow, windowSizeSeconds));
        this.whisperChannelName = whisperChannelName;
        this.twitchGroupServerName = twitchGroupServerName;
        this.twitchGroupServerPort = twitchGroupServerPort;
    }

    public void connect() throws TwitchChatException {
        super.connect(whisperChannelName, twitchGroupServerName, twitchGroupServerPort);
        sendChatHandshake(ChatHandshake.COMMANDS);
        setMessageDelay(messageDelay);
    }
}
