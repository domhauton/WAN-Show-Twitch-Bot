package irc;

import twitch.channel.data.TwitchMessage;
import org.jibble.pircbot.PircBot;

import java.util.function.Consumer;

/**
 * Created by Dominic Hauton on 10/04/2016.
 *
 * Extends PircBot to allow instantiation.
 */
final class PircBotImpl extends PircBot {
    private Consumer<InboundIRCMessage> twitchMessageConsumer;

    PircBotImpl(String name) {
        super();
        setName(name);
    }

    void setTwitchMessageConsumer(Consumer<InboundIRCMessage> consumer) {
        Consumer<TwitchMessage> twitchMessageConsumer;
    }

    @Override
    public void onMessage(String channel, String sender, String login, String hostname, String message) {
        InboundIRCMessage twitchMessage = new InboundIRCMessage(channel, sender, login, hostname, message);
        twitchMessageConsumer.accept(twitchMessage);
    }
}
