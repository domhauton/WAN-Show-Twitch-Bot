package util;

import channel.data.TwitchMessage;

import java.util.function.Consumer;

/**
 * Created by Dominic Hauton on 25/03/2016.
 *
 * Standard interface for a Twitch Message Supplier. Listen is typically used for initialising a connection.
 */
public interface TwitchMessageSupplier {
    /**
     * Start the supplier. May start a connection or start message generation.
     */
    void listen();

    /**
     * An output added here will receive messages given asynchronously.
     * @param twitchMessageConsumer
     */
    void addOutput(Consumer<TwitchMessage> twitchMessageConsumer);
}
