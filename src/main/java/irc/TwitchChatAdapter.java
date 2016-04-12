package irc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jibble.pircbot.IrcException;
import org.joda.time.Period;
import twitch.chat.data.InboundTwitchMessage;
import twitch.chat.exceptions.TwitchChatException;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Created by Dominic Hauton on 10/04/2016.
 *
 * An adapter PircBot that only exposes required methods.
 */
final public class TwitchChatAdapter {
    private Logger log = LogManager.getLogger();

    private PircBotImpl pircBot;

    public TwitchChatAdapter(String twitchUsername) {
        pircBot = new PircBotImpl(twitchUsername);
        pircBot.changeNick(twitchUsername);
    }

    public void setMessageDelay(Period time) {
        pircBot.setMessageDelay(time.toStandardSeconds().getSeconds());
    }

    /**
     * Attempts to establish a connection to the twitch server.
     *
     * @throws TwitchChatException If an error occurred during connection.
     */
    public void connectToTwitchServer(String serverURL, int serverPort, String oAuthToken) throws TwitchChatException {
        try {
            pircBot.connect(serverURL, serverPort, oAuthToken);
        } catch (IOException | IrcException e) {
            String errorMessage = "An IRC error occurred while connecting to TwitchChat Server " + serverURL + ":" +
                                  serverPort + ". Error: " + e.getMessage();
            log.error(errorMessage);
            throw new TwitchChatException(errorMessage);
        }
    }

    public void joinTwitchChannel(String channelName) {
        pircBot.joinChannel(channelName);
    }

    public void sendMessage(String targetChannel, String messagePayload) {
        pircBot.sendMessage(targetChannel, messagePayload);
    }

    public void sendChatHandshake(ChatHandshake chatHandshake) {
        pircBot.sendRawLine(chatHandshake.getHandshake());
    }

    public void setMessageListener(Consumer<InboundTwitchMessage> twitchMessageConsumer) {
        Consumer<InboundIRCMessage> ircMessageConsumer =
                (InboundIRCMessage ircMessage) -> twitchMessageConsumer.accept(convertIRCToTwitchMessage(ircMessage));
        pircBot.setTwitchMessageConsumer(ircMessageConsumer);
    }

    /**
     * Maps an InboundIRCMessage onto an Inbound Twitch Message
     */
    private InboundTwitchMessage convertIRCToTwitchMessage(InboundIRCMessage ircMessage) {
        return new InboundTwitchMessage(ircMessage.getChannel(), ircMessage.getSender(), ircMessage.getMessage());
    }
}
