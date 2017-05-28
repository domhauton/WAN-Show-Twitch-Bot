package twitch.chat.sender;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import irc.ChatHandshake;
import irc.TwitchChatAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import twitch.chat.data.OutboundTwitchMessage;
import twitch.chat.exceptions.TwitchChatException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by Dominic on 04/07/2015.
 *
 * A class for sending messages to twitch functionality to WhisperSender to twitch.
 */
class MessageSender {
  private final static Period MESSAGE_DELAY = new Period(400, PeriodType.millis());

  private Logger log = LogManager.getLogger();
  private String oAuthToken;
  private ExecutorService threadPool;
  private TwitchChatAdapter twitchChatAdapter;

  MessageSender(String twitchUsername, String oAuthToken) {

    log.debug("Creating MessageSender Instance.");
    ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("irc-sender-%d").build();
    threadPool = Executors.newCachedThreadPool(threadFactory);

    twitchChatAdapter = new TwitchChatAdapter(twitchUsername);
    setMessageDelay(MESSAGE_DELAY);
    this.oAuthToken = oAuthToken;
  }

  /**
   * Connects to twitch IRC servers
   */
  void connect(String twitchChannelName, String ircServer, Integer ircPort) throws TwitchChatException {
    twitchChatAdapter.connectToTwitchServer(ircServer, ircPort, oAuthToken);
    log.info("Connected to channel {} on {}:{}", twitchChannelName, ircServer, ircPort);
    if (!Strings.isNullOrEmpty(twitchChannelName)) {
      twitchChatAdapter.joinTwitchChannel(twitchChannelName);
      log.debug("Joined channel " + twitchChannelName);
    } else {
      log.error("Could not join empty twitch channel.");
    }
    sendChatHandshake(ChatHandshake.COMMANDS);
    setMessageDelay(MESSAGE_DELAY);
  }

  private boolean sendMessage(OutboundTwitchMessage outboundTwitchMessage) {
    twitchChatAdapter.sendMessage(outboundTwitchMessage.getTarget(), outboundTwitchMessage.getPayload());
    log.info("Sent Message:\t{}", outboundTwitchMessage);
    return true;
  }

  void sendMessageAsync(OutboundTwitchMessage outboundTwitchMessage) {
    CompletableFuture.runAsync(() -> sendMessage(outboundTwitchMessage), threadPool);
  }

  void sendChatHandshake(ChatHandshake chatHandshake) {
    twitchChatAdapter.sendChatHandshake(chatHandshake);
  }

  void setMessageDelay(Period delay) {
    twitchChatAdapter.setMessageDelay(delay);
  }
}
