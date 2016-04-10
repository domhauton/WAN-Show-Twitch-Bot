package twitchchat.irc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jibble.pircbot.IrcException;
import org.joda.time.Period;
import twitchchat.util.TwitchChatException;

import java.io.IOException;

/**
 * Created by Dominic Hauton on 10/04/2016.
 * <p>
 * A wrapper for PircBot that only exposes required methods.
 */
public class TwitchChatConnector {
    private Logger log = LogManager.getLogger();

    private PircBotImpl pircBot;

    public TwitchChatConnector(String twitchUsername) {
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

    public void sendRawMessage(String messagePayload) {
        pircBot.sendRawLine(messagePayload);
    }
}
