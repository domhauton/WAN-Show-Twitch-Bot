package irc.sender;

import com.google.inject.name.Named;
import irc.util.AsyncEventBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;

/**
 * Created by Dominic on 08/08/2015.
 *
 * Used to send actions and messages
 */
public class PublicMessageSender extends IRCConnection {
    private Logger log = LogManager.getLogger();

    @Inject
    public PublicMessageSender(
            @Named("twitch.username") String twitchUsername,
            @Named("twitch.oauth.token") String oAuthToken,
            @Named("twitch.irc.public.channel") String twitchChannelName,
            @Named("twitch.irc.public.server") String ircServer,
            @Named("twitch.irc.public.port") Integer ircPort,
            @Named("twitch.irc.public.eventCountPerWindow") Integer maxEventCountPerWindow,
            @Named("twitch.irc.public.eventCountWindowSize") Integer windowSizeSeconds) {
        super(twitchUsername, oAuthToken, new AsyncEventBuffer(maxEventCountPerWindow, windowSizeSeconds));
        connect(twitchChannelName, ircServer, ircPort);
        log.debug("Created PublicMessageSender");
    }

    public boolean trySendAction(String recipient, String payload){
        return trySendMessage(recipient, ".me " + payload);
    }

    public void sendActionAsync(String recipient, String payload){
        sendMessageAsync(recipient, ".me " + payload);
    }
}
