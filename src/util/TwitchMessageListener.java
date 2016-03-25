package util;

import channel.message.TwitchMessage;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by Dominic Hauton on 25/03/2016.
 */
public interface TwitchMessageListener {
    void listen();

    void setOutput(Consumer<TwitchMessage> twitchMessageConsumer);
}
