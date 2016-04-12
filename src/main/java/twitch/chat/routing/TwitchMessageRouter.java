package twitch.chat.routing;

import com.google.inject.Inject;
import twitch.chat.data.OutboundTwitchMessage;
import twitch.chat.sender.WhisperSender;
import org.joda.time.Period;

import javax.inject.Singleton;

/**
 * Created by Dominic Hauton on 12/03/2016.
 *
 * An router to control the sending of twitch chat messages.
 */
@Singleton
public class TwitchMessageRouter {
    private WhisperSender whisperSender;
    private ChannelSenderPool channelSenderPool;

    @Inject
    public TwitchMessageRouter(
            WhisperSender whisperSender,
            ChannelSenderPool channelSenderPool) {
        this.whisperSender = whisperSender;
        this.channelSenderPool = channelSenderPool;
    }

    public void sendUserWhisper(OutboundTwitchMessage outboundTwitchMessage) {
        whisperSender.sendWhisperAsync(outboundTwitchMessage);
    }

    public void sendChannelMessage(OutboundTwitchMessage outboundTwitchMessage) {
        channelSenderPool.sendChannelMessage(outboundTwitchMessage);
    }

    public void sendChannelAction(OutboundTwitchMessage outboundTwitchMessage) {
        channelSenderPool.sendChannelAction(outboundTwitchMessage);
    }

    public void timeoutChannelUser(String username, String channel, Period time) {

    }
}
