package channel.message;

import channel.users.TwitchUser;
import org.joda.time.DateTime;

import java.util.Collection;

/**
 * Created by Dominic Hauton on 12/03/2016.
 *
 * Holds required data for a TwitchMessage
 */
public class TwitchMessage {
    private String messagePayload;
    private String simpleMessagePayload;
    private TwitchUser sender;
    private DateTime messageDateTime;

    public TwitchMessage(
            String messagePayload,
            TwitchUser sender,
            DateTime messageDateTime) {
        this.messagePayload = messagePayload;
        this.sender = sender;
        this.messageDateTime = messageDateTime;
    }

    public TwitchMessage(
            String messagePayload,
            String sender,
            DateTime messageDateTime) {
        this.messagePayload = messagePayload;
        this.sender = new TwitchUser(sender);
        this.messageDateTime = messageDateTime;
    }

    public String getMessagePayload() {
        return messagePayload;
    }

    /**
     * Returns the message payload with no spaces in lowercase
     */
    public String getSimpleMessagePayload() {
        if(simpleMessagePayload == null){
            simpleMessagePayload = messagePayload.replaceAll(" ", "").toLowerCase();
        }
        return simpleMessagePayload;
    }

    public TwitchUser getSender() {
        return sender;
    }

    public DateTime getMessageDateTime() {
        return messageDateTime;
    }

    public double getLegalCharRatio(Collection<Character> permittedCharSet) {
        long permittedCharCount = getSimpleMessagePayload().chars()
                .mapToObj(a -> (char)a)
                .filter(permittedCharSet::contains)
                .count();
        return ((double) permittedCharCount) / ((double) getSimpleMessagePayload().length());
    }

    public boolean equalsSimplePayload(String messagePayload) {
        final String simpleMessagePayload = messagePayload.replaceAll(" ", "").toLowerCase();
        return getSimpleMessagePayload().equals(simpleMessagePayload);
    }

    @Override
    public String toString() {
        return String.format("TwitchMessage{[%s] %s: %s}", messageDateTime, sender, messagePayload);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TwitchMessage)) return false;

        TwitchMessage that = (TwitchMessage) o;

        return getMessagePayload() != null ? getMessagePayload().equals(that.getMessagePayload()) : that.getMessagePayload() == null && (getSender() != null ? getSender().equals(that.getSender()) : that.getSender() == null && (getMessageDateTime() != null ? getMessageDateTime().equals(that.getMessageDateTime()) : that.getMessageDateTime() == null));
    }

    @Override
    public int hashCode() {
        int result = getMessagePayload() != null ? getMessagePayload().hashCode() : 0;
        result = 31 * result + (getSender() != null ? getSender().hashCode() : 0);
        result = 31 * result + (getMessageDateTime() != null ? getMessageDateTime().hashCode() : 0);
        return result;
    }
}
