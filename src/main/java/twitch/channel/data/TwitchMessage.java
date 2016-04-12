package twitch.channel.data;

import com.google.common.base.Objects;
import org.joda.time.DateTime;
import twitch.chat.data.InboundTwitchMessage;

import java.util.Collection;

/**
 * Created by Dominic Hauton on 12/03/2016.
 *
 * Holds required data for a TwitchMessage
 */
public class TwitchMessage extends InboundTwitchMessage{
    private String simpleMessagePayload;
    private DateTime messageDateTime;
    private TwitchUser twitchUser;

    public TwitchMessage(
            String message,
            TwitchUser twitchUser,
            DateTime messageDateTime,
            String channel) {
        super(channel, twitchUser.getUsername(), message);
        this.twitchUser = twitchUser;
        this.messageDateTime = messageDateTime;
    }

    /**
     * Returns the message payload with no spaces in lowercase
     */
    public String getSimpleMessagePayload() {
        if(simpleMessagePayload == null){
            simpleMessagePayload = simplifyMessage(super.getMessage());
        }
        return simpleMessagePayload;
    }

    @Override
    public String getUsername() {
        return getTwitchUser().getUsername();
    }

    public TwitchUser getTwitchUser() {
        return twitchUser;
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
        return String.format("TwitchMessage{[%s] %s: %s}", messageDateTime, getUsername(), getMessage());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof TwitchMessage)) { return false; }
        TwitchMessage that = (TwitchMessage) o;
        return Objects.equal(getSimpleMessagePayload(), that.getSimpleMessagePayload()) &&
               Objects.equal(getMessageDateTime(), that.getMessageDateTime()) &&
               Objects.equal(getTwitchUser(), that.getTwitchUser());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getSimpleMessagePayload(), getMessageDateTime(), getTwitchUser());
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
