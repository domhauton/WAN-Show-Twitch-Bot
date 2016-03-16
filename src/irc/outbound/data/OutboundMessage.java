package irc.outbound.data;

import java.time.LocalDateTime;

/**
 * Created by Dominic on 08/08/2015.
 *
 * Stores outbound information info. Used in queues and to store timestamps for analysis.
 */
public class OutboundMessage {
    String payload;
    String recipient;
    LocalDateTime created;
    LocalDateTime sent;

    public OutboundMessage(String recipient, String payload){
        this.payload = payload;
        this. recipient = recipient;
        created = LocalDateTime.now();
    }

    public void setMessageSent(){
        if(sent == null) sent = LocalDateTime.now();
    }

    public String getPayload() {
        return payload;
    }

    public String getRecipient() {
        return recipient;
    }

    public LocalDateTime getSentTime() {
        return sent;
    }
}
