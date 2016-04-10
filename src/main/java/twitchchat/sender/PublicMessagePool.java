package twitchchat.sender;

import twitchchat.util.OutboundMessage;

import javax.inject.Inject;

/**
 * Created by Dominic Hauton on 16/03/2016.
 *
 * Will manage PublicMessageSender instances to allow constant message sending.
 */
public class PublicMessagePool {
    private IRCConnection ircConnection;

    @Inject
    public PublicMessagePool(IRCConnection ircConnection) {
        this.ircConnection = ircConnection;
    }

    public void sendChannelMessage(OutboundMessage outboundMessage) {
        ircConnection.sendMessageAsync(outboundMessage);
    }
}
