package irc.outbound;

import irc.IRCConnection;
import irc.info.ServerInfo;
import irc.outbound.data.OutboundMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Dominic on 08/08/2015.
 *
 * Used to send actions and messages
 */
public class IRCPublic extends IRCConnection {
    private Logger log = LogManager.getLogger();
    
    public IRCPublic(ServerInfo serverInfo) {
        super(serverInfo);
        log.debug("Created IRC public");
    }

    public boolean sendMessage(OutboundMessage outboundMessage){
        try {
            addMessageToBuffer(outboundMessage);
        } catch (IllegalStateException e){
            return false;
        }
        sendMessage(outboundMessage.getRecipient(), outboundMessage.getPayload());
        log.debug("Sent Message:\t{}\t{}", outboundMessage.getRecipient(), outboundMessage.getPayload());
        return true;
    }

    public boolean sendAction(OutboundMessage outboundMessage){
        try {
            addMessageToBuffer(outboundMessage);
        } catch (IllegalStateException e){
            return false;
        }
        sendMessage(outboundMessage.getRecipient(), ".me " + outboundMessage.getPayload());
        log.debug("Sent Action:\t{}\t{}", outboundMessage.getRecipient(), outboundMessage.getPayload());
        return true;
    }
}
