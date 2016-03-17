package irc;

import com.google.inject.name.Named;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Dominic Hauton on 16/03/2016.
 */
@Singleton
public class MessageSenderFactory {
    private String twitchUsername;
    private String twitchChannelName;
    private String oAuthToken;
    private String ircServer;
    private Integer ircPort;

    @Inject
    public MessageSenderFactory(@Named("twitch.irc.channel") String twitchChannelName,
                                @Named("twitch.username") String twitchUsername,
                                @Named("twitch.oauth.token") String oAuthToken,
                                @Named("twitch.irc.server") String ircServer,
                                @Named("twitch.irc.port") Integer ircPort) {
        this.twitchUsername = twitchUsername;
        this.twitchChannelName = twitchChannelName;
        this.oAuthToken = oAuthToken;
        this.ircServer = ircServer;
        this.ircPort = ircPort;
    }
}
