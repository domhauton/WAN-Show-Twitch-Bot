package channel.message;

import com.google.common.collect.ImmutableSet;
import org.joda.time.DateTime;
import channel.users.TwitchUser;

import java.util.Arrays;
import java.util.stream.Stream;

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

    public double getLegalCharRatio(ImmutableSet<Character> permittedCharSet) {
        long permittedCharCount = getMessagePayload().chars()
                .mapToObj(a -> (char)a)
                .filter(permittedCharSet::contains)
                .count();
        return ((double) permittedCharCount) / ((double) messagePayload.length());
    }
}
