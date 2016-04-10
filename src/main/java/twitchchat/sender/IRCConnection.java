package twitchchat.sender;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.joda.time.Period;
import twitchchat.util.AsyncEventBuffer;
import twitchchat.util.OutboundMessage;
import twitchchat.irc.TwitchChatConnector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitchchat.util.TwitchChatException;

import java.util.concurrent.*;

/**
 * Created by Dominic on 04/07/2015.
 *
 * A class for sending messages to twitch functionality to connect to twitch.
 */
class IRCConnection{

    private Logger log = LogManager.getLogger();
    private String oAuthToken;
    private ExecutorService threadPool;

    protected TwitchChatConnector twitchChatConnector;

    private AsyncEventBuffer asyncEventBuffer;

    IRCConnection(
            String twitchUsername,
            String oAuthToken,
            AsyncEventBuffer asyncEventBuffer){

        log.debug("Creating IRCConnection Instance.");
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("irc-sender-%d").build();
        threadPool = Executors.newCachedThreadPool(threadFactory);

        twitchChatConnector = new TwitchChatConnector(twitchUsername);
        twitchChatConnector.setMessageDelay(Period.ZERO);

        this.asyncEventBuffer = asyncEventBuffer;
        this.oAuthToken = oAuthToken;
    }

    /**
     * Connects to twitch IRC servers
     */
    void connect(String twitchChannelName, String ircServer, Integer ircPort) throws TwitchChatException{
        twitchChatConnector.connectToTwitchServer(ircServer, ircPort, oAuthToken);
        log.info("Connected to channel {} on {}:{}", twitchChannelName, ircServer, ircPort);
        if(!Strings.isNullOrEmpty(twitchChannelName)) {
            twitchChatConnector.joinTwitchChannel(twitchChannelName);
            log.debug("Joined channel " + twitchChannelName);
        } else {
            log.error("Could not join empty twitch channel.");
        }
    }

    private boolean trySendMessage(OutboundMessage outboundMessage){
        if(asyncEventBuffer.addMessage()) {
            twitchChatConnector.sendMessage(outboundMessage.getTarget(), outboundMessage.getPayload());
            log.info("Sent Message:\t{}", outboundMessage);
            return true;
        } else {
            log.trace("Event Buffer rejected message:\t{}\t{}");
            return false;
        }
    }

    void sendMessageAsync(OutboundMessage outboundMessage) {
        CompletableFuture.runAsync(() -> {
            while(!trySendMessage(outboundMessage)){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, threadPool);
    }
}
