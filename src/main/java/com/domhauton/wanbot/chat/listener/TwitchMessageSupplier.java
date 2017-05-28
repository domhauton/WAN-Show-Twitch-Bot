package com.domhauton.wanbot.chat.listener;

import com.domhauton.wanbot.chat.data.InboundTwitchMessage;

import java.util.function.Consumer;

/**
 * Created by Dominic Hauton on 25/03/2016.
 *
 * Standard interface for a Twitch Message Supplier. Listen is typically used for initialising a
 * connection.
 */
interface TwitchMessageSupplier {
  /**
   * com.domhauton.wanbot.bot.Start the supplier. May start a connection or start MESSAGE generation.
   */
  void listen();

  /**
   * An output added here will receive messages given asynchronously.
   */
  void addOutput(Consumer<InboundTwitchMessage> twitchMessageConsumer);
}
