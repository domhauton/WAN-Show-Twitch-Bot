package com.domhauton.wanbot.chat.listener;

import com.domhauton.wanbot.chat.data.InboundTwitchMessage;
import com.domhauton.wanbot.chat.exceptions.TwitchChatException;
import com.domhauton.wanbot.config.TwitchInfo;
import com.domhauton.wanbot.irc.ChatHandshake;
import com.domhauton.wanbot.irc.TwitchChatAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Period;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;

/**
 * Created by Dominic Hauton on 25/03/2016.
 * <p>
 * Connects to a given TwitchChannel and listens for messages.
 */
public class TwitchChannelListener implements TwitchMessageSupplier {

  private Logger log = LogManager.getLogger();

  private Collection<Consumer<InboundTwitchMessage>> messageConsumers;

  private TwitchChatAdapter twitchChatAdapter;

  private String twitchUsername;
  private String twitchChannelName;
  private String oAuthToken;
  private String ircServer;
  private Integer ircPort;

  public TwitchChannelListener(TwitchInfo twitchInfo) {
    this(twitchInfo.getChannel().getChannelName(),
        twitchInfo.getUsername(),
        twitchInfo.getoAuth(),
        twitchInfo.getChannel().getHostname(),
        twitchInfo.getChannel().getPort());
  }

  TwitchChannelListener(String twitchChannelName, String twitchUsername, String oAuthToken,
                        String ircServer, Integer ircPort) {
    log.info("Starting com.domhauton.wanbot.bot for channel {} on server {}", twitchChannelName, ircServer);

    this.twitchChannelName = twitchChannelName;
    this.twitchUsername = twitchUsername;
    this.oAuthToken = oAuthToken;
    this.ircServer = ircServer;
    this.ircPort = ircPort;

    twitchChatAdapter = new TwitchChatAdapter(twitchUsername);
    twitchChatAdapter.setMessageDelay(Period.millis(50));

    messageConsumers = new HashSet<>();
  }

  @Override
  public void listen() {
    log.info("Listener connecting to twitch com.domhauton.wanbot.irc servers at {}@{}:{}", twitchUsername, ircServer, ircPort);
    try {
      twitchChatAdapter.connectToTwitchServer(ircServer, ircPort, oAuthToken);
    } catch (TwitchChatException e) {
      log.error("Listener failed to connect to {}:{}", ircServer, ircPort);
    }
    twitchChatAdapter.sendChatHandshake(ChatHandshake.MEMBERSHIP);
    twitchChatAdapter.joinTwitchChannel(twitchChannelName);
    twitchChatAdapter.setMessageListener(this::onInboundTwitchMessage);
    log.info("Listener connected successfully to channel {} on {}@{}:{}", twitchChannelName, twitchUsername, ircServer, ircPort);
  }

  @Override
  public void addOutput(Consumer<InboundTwitchMessage> twitchMessageConsumer) {
    messageConsumers.add(twitchMessageConsumer);
  }

  private void onInboundTwitchMessage(InboundTwitchMessage inboundTwitchMessage) {
    messageConsumers.parallelStream().forEach(messageConsumers -> messageConsumers.accept(inboundTwitchMessage));
  }
}
