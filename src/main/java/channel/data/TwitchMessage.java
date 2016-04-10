package channel.data;

import com.google.common.base.Objects;
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
    private String sourceChannel;

    public TwitchMessage(
            String messagePayload,
            TwitchUser sender,
            DateTime messageDateTime,
            String sourceChannel) {
        this.messagePayload = messagePayload;
        this.sender = sender;
        this.messageDateTime = messageDateTime;
        this.sourceChannel = sourceChannel;
    }

    public String getMessagePayload() {
        return messagePayload;
    }

    /**
     * Returns the message payload with no spaces in lowercase
     */
    public String getSimpleMessagePayload() {
        if(simpleMessagePayload == null){
            simpleMessagePayload = simplifyMessage(messagePayload);
        }
        return simpleMessagePayload;
    }

    public TwitchUser getSender() {
        return sender;
    }

    public String getSenderUsername() {
        return sender.getUsername();
    }

    public String getSourceChannel() {
        return sourceChannel;
    }

    public DateTime getMessageDateTime() {
        return messageDateTime;
    }

    /**
     * Returns the ratio of legal chars in the simple message payload to number of chars in the simplified message.
     * @param permittedCharSet Chars that are counted as legal.
     * @return Double between 0 and 1 that shows what proportion of chars are legal.
     */
    public double getLegalCharRatio(Collection<Character> permittedCharSet) {
        long permittedCharCount = getSimpleMessagePayload().chars()
                .mapToObj(a -> (char)a)
                .filter(permittedCharSet::contains)
                .count();
        return ((double) permittedCharCount) / ((double) getSimpleMessagePayload().length());
    }

    /**
     * True if the given payload is the same as the message once both are simplified.
     * @param messagePayload payload to compare to this message.
     * @return true if exactly the same.
     */
    public boolean equalsSimplePayload(String messagePayload) {
        String simpleMessagePayload = simplifyMessage(messagePayload);
        return getSimpleMessagePayload().equals(simpleMessagePayload);
    }

    /**
     * True if the message contains the simplified given string.
     * @param stringToMatch String to match. Spaces and case removed
     * @return true if match.
     */
    public boolean containsString(String stringToMatch) {
        return getSimpleMessagePayload().contains(simplifyMessage(stringToMatch));
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
        return Objects.equal(getMessagePayload(), that.getMessagePayload()) &&
                Objects.equal(getSimpleMessagePayload(), that.getSimpleMessagePayload()) &&
                Objects.equal(sender, that.sender) &&
                Objects.equal(getMessageDateTime(), that.getMessageDateTime());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getMessagePayload(), getSimpleMessagePayload(), sender, getMessageDateTime());
    }

    /**
     * Simplifies message for easier processing
     * @param originalMessage payload of message to simplify
     * @return simplified message
     */
    private static String simplifyMessage(String originalMessage) {
        return originalMessage.replaceAll(" ", "").toLowerCase();
    }
}
