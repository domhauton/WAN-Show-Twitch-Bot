package twitchchat;

import com.google.inject.Inject;
import twitchchat.sender.PrivateMessageSender;
import twitchchat.sender.PublicMessagePool;
import twitchchat.util.OutboundMessage;
import org.joda.time.Period;

import javax.inject.Singleton;

/**
 * Created by Dominic Hauton on 12/03/2016.
 *
 * An router to control the sending of twitch chat messages.
 */
@Singleton
public class TwitchMessageRouter {
    private PrivateMessageSender privateMessageSender;
    private PublicMessagePool publicMessagePool;

    @Inject
    public TwitchMessageRouter(
            PrivateMessageSender privateMessageSender,
            PublicMessagePool publicMessagePool) {
        this.privateMessageSender = privateMessageSender;
        this.publicMessagePool = publicMessagePool;
    }

    public void sendUserWhisper(OutboundMessage outboundMessage) {
        privateMessageSender.sendWhisperAsync(outboundMessage);
    }

    public void sendChannelMessage(OutboundMessage outboundMessage) {
        publicMessagePool.sendChannelMessage(outboundMessage);
    }

    public void sendChannelAction(String channel, String action) {
        String formattedPayload = ".me " + action;
        OutboundMessage formattedAction = new OutboundMessage(formattedPayload, channel);
        publicMessagePool.sendChannelAction(formattedAction);
    }

    public void timeoutChannelUser(String username, String channel, Period time) {

    }
}
