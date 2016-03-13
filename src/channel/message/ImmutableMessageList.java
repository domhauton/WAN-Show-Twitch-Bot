package channel.message;

import channel.users.TwitchUser;
import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.Duration;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by Dominic Hauton on 13/03/2016.
 */
public class ImmutableMessageList {
    private ImmutableList<TwitchMessage> twitchMessages;

    public ImmutableMessageList(Collection<TwitchMessage> twitchMessages) {
        this.twitchMessages = ImmutableList.copyOf(twitchMessages);
    }

    public ImmutableList<TwitchMessage> getTwitchMessages() {
        return twitchMessages;
    }

    public ImmutableMessageList filterUser(TwitchUser twitchUser) {
        return new ImmutableMessageList( twitchMessages.stream()
                .filter( twitchMessage -> twitchMessage.getSender().equals(twitchUser) )
                .collect(Collectors.toList()));
    }

    public int size(){
        return twitchMessages.size();
    }

    /**
     * Finds the time span of the messages in the list
     *
     * @return Length of time in seconds
     */
    public Double getTimeSpanSeconds(){
        if (twitchMessages.size() <= 0) return 0D;
        DateTime minDateTime = twitchMessages.stream().map(TwitchMessage::getMessageDateTime).min(DateTimeComparator.getInstance()).get();
        DateTime maxDateTime = twitchMessages.stream().map(TwitchMessage::getMessageDateTime).max(DateTimeComparator.getInstance()).get();
        long timeLength = new Duration(minDateTime, maxDateTime).getStandardSeconds();
        return (double) twitchMessages.size() / (double) timeLength;
    }
}
