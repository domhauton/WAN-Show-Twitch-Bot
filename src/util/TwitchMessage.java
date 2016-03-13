package util;

import org.joda.time.DateTime;
import channel.users.TwitchUser;

/**
 * Created by Dominic Hauton on 12/03/2016.
 */
public class TwitchMessage {
    private String messagePayload;
    private TwitchUser sender;
    private DateTime messageDateTime;
    private String channel;
    private String hostname;

    public TwitchMessage(String messagePayload, TwitchUser sender, DateTime messageDateTime, String channel, String hostname) {
        this.messagePayload = messagePayload;
        this.sender = sender;
        this.messageDateTime = messageDateTime;
        this.channel = channel;
        this.hostname = hostname;
    }

    public String getMessagePayload() {
        return messagePayload;
    }

    public TwitchUser getSender() {
        return sender;
    }

    public DateTime getMessageDateTime() {
        return messageDateTime;
    }


}
