package irc.sender;

import com.google.common.base.Strings;
import irc.util.EventBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jibble.pircbot.PircBot;

/**
 * Created by Dominic on 04/07/2015.
 *
 * An abstract class that implements default functionality to connect to twitch.
 */
public abstract class IRCConnection extends PircBot {

    private Logger log = LogManager.getLogger();
    private String oAuthToken;

    private EventBuffer eventBuffer;

    public IRCConnection(
            String twitchUsername,
            String oAuthToken,
            EventBuffer eventBuffer){
        super();
        log.debug("Creating IRCConnection Instance.");
        setName(twitchUsername);
        changeNick(twitchUsername);
        setMessageDelay(0);

        this.eventBuffer = eventBuffer;
        this.oAuthToken = oAuthToken;
    }

    /**
     * Connects to twitch IRC servers
     */
    public void connect(String twitchChannelName, String ircServer, Integer ircPort){
        try {
            super.connect(ircServer, ircPort, oAuthToken);
            log.debug("Connected to {}:{}", ircServer, ircPort);
            if(!Strings.isNullOrEmpty(twitchChannelName)) {
                joinChannel(twitchChannelName);
                log.debug("Joined channel " + twitchChannelName);
            }
        } catch (Exception e){
            log.error("ERR - Could not connect to twitch: " + e.getMessage());
        }
    }

    public boolean trySendMessage(String recipient, String payload){
        if(eventBuffer.addMessage()) {
            sendMessage(recipient, payload);
            log.debug("Sent Message:\t{}\t{}", recipient, payload);
            return true;
        } else {
            log.trace("Event Buffer rejected message:\t{}\t{}");
            return false;
        }
    }
}
