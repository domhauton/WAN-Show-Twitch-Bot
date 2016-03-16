package irc.inbound;

import irc.IRCConnection;
import irc.info.ServerInfo;
import message.controllers.InboundRouter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jibble.pircbot.User;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Dominic on 08/08/2015.
 *
 * A connection that sits in the channel and listens to messages. You should not send messages using this.
 */
public class IRCListener extends IRCConnection {
    private InboundRouter inboundRouter;
    private Logger log = LogManager.getLogger();

    public IRCListener(ServerInfo serverInfo) {
        super(serverInfo);
        log.debug("Created IRC Listener class");
    }

    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        //Channel Message:	#lttghost - ltt_bot - ltt_bot - ltt_bot.tmi.twitch.tv - JUnit Test Message Channel
        log.debug("Received Channel Message:\t{} - {} - {} - {} - {}", channel, sender, login, hostname, message);
        if(inboundRouter == null) {
            log.error("Message arrived before inboundRouter set");
            return;
        }
        inboundRouter.routeMessage(sender, message);
    }

    @Override
    protected void onUserList(String channel, User[] users) {
        log.debug("Received User List. {} user/s.", users.length);
        log.debug(Arrays.stream(users)
                .map(User::getNick)
                .collect(Collectors.joining(", ", "Users :\t", "")));
        Set<String> userSet = Arrays.stream(users).map(User::getNick).collect(Collectors.toSet());
        inboundRouter.userList(channel, userSet);
    }

    @Override
    protected void onAction(String sender, String login, String hostname, String target, String action) {
        //Action:		ltt_bot - ltt_bot - ltt_bot.tmi.twitch.tv - #lttghost - JUnit Test Action
        log.debug("Received  Action:\t\t{} - {} - {} - {} - {}", sender, login, hostname, target, action);
        if(inboundRouter == null) {
            log.error("Message arrived before inboundRouter set");
            return;
        }
        inboundRouter.routeAction(login, action);
    }

    public void setInboundRouter(InboundRouter inboundRouter) {
        this.inboundRouter = inboundRouter;
    }
}
