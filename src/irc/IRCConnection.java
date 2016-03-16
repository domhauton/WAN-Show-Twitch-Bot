package irc;

import irc.info.ServerInfo;
import irc.outbound.data.OutboundMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jibble.pircbot.PircBot;

import java.time.LocalDateTime;
import java.util.LinkedList;

/**
 * Created by Dominic on 04/07/2015.
 *
 * An abstract class that implements default functionality to connect to twitch.
 */
public abstract class IRCConnection extends PircBot {

    private LinkedList<OutboundMessage> messageQueue;
    private Logger log = LogManager.getLogger();

    protected ServerInfo serverInfo;

    public IRCConnection(ServerInfo serverInfo){
        super();
        log.debug("Creating IRCConnection Instance.");
        this.serverInfo = serverInfo;
        setName(serverInfo.getLoginInfo().getUsername());
        changeNick(serverInfo.getLoginInfo().getUsername());
        setMessageDelay(0);
        messageQueue = new LinkedList<>();
        connect(serverInfo.getServerIP(), serverInfo.getPort(), serverInfo.getLoginInfo().getPassword(), serverInfo.getChannelName());
    }

    /**
     * Connects to twitch IRC servers
     */
    public void connect(String ip, Integer port, String oauth, String channel){
        try {
            super.connect(ip, port, oauth);
            log.debug("Connected to " + ip + ":" + port);
            if(!channel.equals("")) {
                joinChannel(channel);
                log.debug("Joined channel " + channel);
            }
        } catch (Exception e){
            log.error("ERR - Could not connect to twitch: " + e.getMessage());
        }
    }

    /**
     * Adds a message to the current queue
     * @throws IllegalStateException if message is added when buffer is full
     */
    public synchronized void addMessageToBuffer(OutboundMessage newMessage) throws IllegalStateException{
        if (isMessageBufferFull()) throw new IllegalStateException("Message Buffer Full");
        newMessage.setMessageSent();
        messageQueue.add(newMessage);
    }

    /**
     * @return true if message buffer is true
     */
    public synchronized boolean isMessageBufferFull(){
        clearQueue();
        return messageQueue.size() >= serverInfo.getMessageRate();
    }

    private synchronized void clearQueue(){
        LocalDateTime expiryTime = LocalDateTime.now().minusSeconds(serverInfo.getBurstPeriod());
        while(!messageQueue.isEmpty()){
            if(messageQueue.peekFirst().getSentTime().isBefore(expiryTime)) messageQueue.removeFirst();
            else return;
        }
    }
}
