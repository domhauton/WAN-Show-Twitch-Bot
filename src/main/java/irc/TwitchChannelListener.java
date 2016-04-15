package irc;

import channel.data.TwitchMessage;
import channel.data.TwitchUser;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import org.joda.time.DateTime;
import util.TwitchMessageSupplier;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by Dominic Hauton on 25/03/2016.
 *
 * Connects to a given TwitchChannel and listens for messages.
 */
public class TwitchChannelListener extends PircBot implements TwitchMessageSupplier {

    private Logger log = LogManager.getLogger();

    private Collection<Consumer<TwitchMessage>> messageConsumers;

    private String twitchUsername;
    private String twitchChannelName;
    private String oAuthToken;
    private String ircServer;
    private Integer ircPort;

    @Inject
    public TwitchChannelListener(
            @Named("twitch.irc.public.twitchChannel") String twitchChannelName,
            @Named("twitch.username") String twitchUsername,
            @Named("twitch.oauth.token") String oAuthToken,
            @Named("twitch.irc.public.server") String ircServer,
            @Named("twitch.irc.public.port") Integer ircPort
    ) {
        log.info("Starting bot for channel {} on server {}", twitchChannelName, ircServer);

        this.twitchChannelName = twitchChannelName;
        this.twitchUsername = twitchUsername;
        this.oAuthToken = oAuthToken;
        this.ircServer = ircServer;
        this.ircPort = ircPort;

        setName(twitchUsername);
        setMessageDelay(50);

        messageConsumers = new HashSet<>();
    }

    @Override
    public void listen() {
        log.info("Listener connecting to twitch irc servers at {}@{}:{}", twitchUsername, ircServer, ircPort);
        try {
            super.connect(ircServer, ircPort, oAuthToken);
            sendRawLine("CAP REQ :twitch.tv/membership");
            log.info( "Channel available: {}", Stream.of( super.getChannels() ).anyMatch(twitchChannelName::equals) ? "True" : "False" );
            super.joinChannel(twitchChannelName);

        } catch (IOException e) {
            log.fatal("Failed to connect to twitch due to IOException: {}", e.getMessage());
            throw new UncheckedIOException(e);
        } catch (IrcException e) {
            log.fatal("Failed to connect to twitch due to IRCException: {}", e.getMessage());
            throw new UncheckedExecutionException(e);
        }
        log.info("Listener connected successfully to channel {} on {}@{}:{}", twitchChannelName, twitchUsername, ircServer, ircPort);
    }

    @Override
    public void addOutput(Consumer<TwitchMessage> twitchMessageConsumer) {
        messageConsumers.add(twitchMessageConsumer);
    }

    /**
     * Overrides PircBot on Message to handle message correctly.
     */
    @Override
    public void onMessage(String channel, String sender, String login, String hostname, String message) {
        final TwitchMessage twitchMessage = new TwitchMessage(message, new TwitchUser(sender), DateTime.now());
        messageConsumers.parallelStream().forEach(messageConsumers -> messageConsumers.accept(twitchMessage));
    }
}
