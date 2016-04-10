package irc.sender;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import irc.util.AsyncEventBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jibble.pircbot.PircBot;

import java.util.concurrent.*;

/**
 * Created by Dominic on 04/07/2015.
 *
 * An abstract class that implements default functionality to connect to twitch.
 */
public abstract class IRCConnection extends PircBot {

    private Logger log = LogManager.getLogger();
    private String oAuthToken;
    private ExecutorService threadPool;

    private AsyncEventBuffer asyncEventBuffer;

    public IRCConnection(
            String twitchUsername,
            String oAuthToken,
            AsyncEventBuffer asyncEventBuffer){
        super();
        log.debug("Creating IRCConnection Instance.");
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("irc-sender-%d").build();
        threadPool = Executors.newCachedThreadPool(threadFactory);
        setName(twitchUsername);
        changeNick(twitchUsername);
        setMessageDelay(0);

        this.asyncEventBuffer = asyncEventBuffer;
        this.oAuthToken = oAuthToken;
    }

    /**
     * Connects to twitch IRC servers
     */
    public void connect(String twitchChannelName, String ircServer, Integer ircPort){
        try {
            super.connect(ircServer, ircPort, oAuthToken);
            log.info("Connected to channel {} on {}:{}", twitchChannelName, ircServer, ircPort);
            if(!Strings.isNullOrEmpty(twitchChannelName)) {
                joinChannel(twitchChannelName);
                log.debug("Joined channel " + twitchChannelName);
            }
        } catch (Exception e){
            log.error("ERR - Could not connect to twitch channel {} on server {}:{}. Error: {}" ,twitchChannelName, ircServer, ircPort, e.getMessage());
        }
    }

    public boolean trySendMessage(String recipient, String payload){
        if(asyncEventBuffer.addMessage()) {
            sendMessage(recipient, payload);
            log.info("Sent Message:\t{}\t{}", recipient, payload);
            return true;
        } else {
            log.trace("Event Buffer rejected message:\t{}\t{}");
            return false;
        }
    }

    public void sendMessageAsync(String recipient, String payload) {
        CompletableFuture.runAsync(() -> {
            while(!trySendMessage(recipient, payload)){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, threadPool);
    }
}
