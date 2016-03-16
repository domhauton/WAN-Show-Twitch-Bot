package irc.outbound;

import irc.IRCConnection;
import irc.info.ServerInfo;
import irc.outbound.data.OutboundMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Dominic on 08/08/2015.
 *
 * Used to send whispers to other users.
 */
public class IRCWhisper extends IRCConnection {

    private final Logger log = LogManager.getLogger();
    private static final Long s_MessageDelay = 400L;
    private static final String s_HandShakeContent = "CAP REQ :twitch.tv/commands";

    public IRCWhisper(ServerInfo serverInfo) {
        super(serverInfo);
        OutboundMessage handshakeMessage = new OutboundMessage("", s_HandShakeContent);
        try {
            addMessageToBuffer(handshakeMessage);
        } catch (IllegalStateException e){
            log.error("Could not send twitch command message to enable whispering");
        }
        sendRawLine(handshakeMessage.getPayload());
        log.debug("Sent handshake message: {}", handshakeMessage.getPayload());
        setMessageDelay(s_MessageDelay);
        log.debug("Created IRCWhisper class");
    }

    /**
     * @return return false if message could not be sent due to full queue
     */
    public boolean sendWhisper(OutboundMessage message){
        log.debug("Sent Whisper:\t{}\t{}", message.getRecipient(), message.getPayload());
        String formattedMessage = String.format(".w %s %s", message.getRecipient(), message.getPayload());
        try {
            addMessageToBuffer(message);
        } catch (IllegalStateException e){
            return false;
        }
        super.sendMessage(serverInfo.getChannelName(), formattedMessage);
        return true;
    }
}
