package irc.outbound;

import irc.outbound.data.OutboundMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jibble.pircbot.PircBot;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Created by Dominic H on 08/08/2015 at 18:09.
 *
 * Used to open outbound m_connections a route messages to the appropriate connection
 */
public class OutboundMessageRouter {
    private ArrayList<IRCPublic> m_connections;
    private IRCWhisper m_ircWhisper;
    private final Logger log = LogManager.getLogger();

    //Dependencies
    private final Supplier<IRCPublic> ircPublicSupplier;
    private final Supplier<IRCWhisper> whisperSupplier;

    private Integer m_maxSendAttempts = 5;

    public OutboundMessageRouter(Supplier<IRCPublic> ircPublicSupplier, Supplier<IRCWhisper> ircWhisperSupplier) {
        this.ircPublicSupplier = ircPublicSupplier;
        this.whisperSupplier = ircWhisperSupplier;
        m_connections = new ArrayList<>();
        createNewIRCWhisper();
    }

    public synchronized void sendWhisper(OutboundMessage outboundMessage) {
        while (true) {
            if (m_ircWhisper.sendWhisper(outboundMessage)) return;
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void sendMessage(OutboundMessage outboundMessage) {
        for (int attempts = 0; attempts < m_maxSendAttempts; attempts++) {
            if (getFreePublic().sendMessage(outboundMessage)) return;
        }
        log.error("Failed to send message:\t" + outboundMessage.getPayload());
    }

    public synchronized void sendAction(OutboundMessage outboundMessage) {
        for (int attempts = 0; attempts < m_maxSendAttempts; attempts++) {
            if (getFreePublic().sendAction(outboundMessage)) return;
        }
        log.error("Failed to send action:\t" + outboundMessage.getPayload());
    }

    private IRCPublic getFreePublic() {
        Optional<IRCPublic> connection = m_connections.stream()
                .filter(a -> !a.isMessageBufferFull())
                .findAny();
        return connection.isPresent() ? connection.get() : createNewPublic();
    }

    private IRCPublic createNewPublic() {
        log.debug("Creating IRCPublic");
        IRCPublic ircConnection = ircPublicSupplier.get();
        m_connections.add(ircConnection);
        return ircConnection;
    }

    private IRCWhisper createNewIRCWhisper() {
        log.debug("Creating IRCWhisper");
        IRCWhisper ircConnection = whisperSupplier.get();
        m_ircWhisper = ircConnection;
        return ircConnection;
    }

    public void waitTillIdle(){
        while(m_connections.stream()
                .filter(a -> a.getOutgoingQueueSize() > 0)
                .count() > 0){
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                log.error("Sleep interrupted");
            }
        }
    }

    public synchronized void disconnectAll() throws InterruptedException {
        while (countActiveConnections() > 0){
            m_connections.stream()
                    .filter(a -> a.getOutgoingQueueSize() <= 0)
                    .filter(PircBot::isConnected)
                    .forEach(PircBot::disconnect);
            Thread.sleep(250);
        }
        m_connections.clear();
        if (m_ircWhisper != null) {
            while(m_ircWhisper.getOutgoingQueueSize() > 0) Thread.sleep(250);
            m_ircWhisper.disconnect();
            m_ircWhisper = null;
        }
    }

    /**
     * @return number of active m_connections (ignoring whisper connection)
     */
    private Long countActiveConnections(){
        return m_connections.stream()
                .filter(a -> !a.isConnected())
                .count();
    }

    public void setM_ircWhisper(IRCWhisper m_ircWhisper) {
        this.m_ircWhisper = m_ircWhisper;
    }

    public void setM_maxSendAttempts(Integer m_maxSendAttempts) {
        this.m_maxSendAttempts = m_maxSendAttempts;
    }
}
