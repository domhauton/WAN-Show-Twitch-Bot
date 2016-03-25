package channel.message;

import channel.users.TwitchUser;
import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.Duration;
import org.joda.time.Period;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Dominic Hauton on 13/03/2016.
 *
 * Decorator for ImmutableList providing some extra functionality
 */
public class ImmutableTwitchMessageList {
    private ImmutableList<TwitchMessage> twitchMessages;

    public ImmutableTwitchMessageList(Collection<TwitchMessage> twitchMessages) {
        if(twitchMessages==null) {
            this.twitchMessages = new ImmutableList.Builder<TwitchMessage>().build();
        } else {
            this.twitchMessages = ImmutableList.copyOf(twitchMessages);
        }
    }

    public Stream<TwitchMessage> stream() {
        return twitchMessages.stream();
    }

    public ImmutableTwitchMessageList filterUser(TwitchUser twitchUser) {
        return new ImmutableTwitchMessageList( twitchMessages.stream()
                .filter( twitchMessage -> twitchMessage.getSender().equals(twitchUser) )
                .collect(Collectors.toList()));
    }

    public long containsSimplePayload(String payload) {
        return stream().filter(twitchMessage -> twitchMessage.equalsSimplePayload(payload)).count();
    }

    public int size(){
        return twitchMessages.size();
    }

    /**
     * Finds the time span of the messages in the list
     *
     * @return Length of time in seconds
     */
    public Period getMessageTimePeriod(){
        if (twitchMessages.size() <= 0) return Period.ZERO;
        DateTime minDateTime = twitchMessages.stream().map(TwitchMessage::getMessageDateTime).min(DateTimeComparator.getInstance()).get();
        DateTime maxDateTime = twitchMessages.stream().map(TwitchMessage::getMessageDateTime).max(DateTimeComparator.getInstance()).get();
        return new Period(minDateTime, maxDateTime);
    }

    @Override
    public String toString() {
        return twitchMessages.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImmutableTwitchMessageList)) return false;

        ImmutableTwitchMessageList that = (ImmutableTwitchMessageList) o;

        return twitchMessages != null ? twitchMessages.equals(that.twitchMessages) : that.twitchMessages == null;

    }

    @Override
    public int hashCode() {
        return twitchMessages != null ? twitchMessages.hashCode() : 0;
    }
}
