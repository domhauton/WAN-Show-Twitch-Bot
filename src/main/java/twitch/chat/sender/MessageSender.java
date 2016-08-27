package twitch.chat.sender;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Period;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import irc.ChatHandshake;
import irc.TwitchChatAdapter;
import twitch.chat.data.OutboundTwitchMessage;
import twitch.chat.exceptions.TwitchChatException;

/**
 * Created by Dominic on 04/07/2015.
 *
 * A class for sending messages to twitch functionality to WhisperSender to twitch.
 */
abstract class MessageSender {

  private Logger log = LogManager.getLogger();
  private String oAuthToken;
  private ExecutorService threadPool;
  private TwitchChatAdapter twitchChatAdapter;

  private AsyncEventBuffer asyncEventBuffer;

  MessageSender(
      String twitchUsername,
      String oAuthToken,
      AsyncEventBuffer asyncEventBuffer) {

    log.debug("Creating MessageSender Instance.");
    ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("irc-sender-%d").build();
    threadPool = Executors.newCachedThreadPool(threadFactory);

    twitchChatAdapter = new TwitchChatAdapter(twitchUsername);
    setMessageDelay(Period.ZERO);

    this.asyncEventBuffer = asyncEventBuffer;
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
  }

  private boolean trySendMessage(OutboundTwitchMessage outboundTwitchMessage) {
    if (asyncEventBuffer.addMessage()) {
      twitchChatAdapter.sendMessage(outboundTwitchMessage.getTarget(), outboundTwitchMessage.getPayload());
      log.info("Sent Message:\t{}", outboundTwitchMessage);
      return true;
    } else {
      log.trace("Event Buffer rejected MESSAGE:\t{}\t{}");
      return false;
    }
  }

  void sendMessageAsync(OutboundTwitchMessage outboundTwitchMessage) {
    CompletableFuture.runAsync(() -> {
      //FIXME Loop may not exit
      while (!trySendMessage(outboundTwitchMessage)) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }, threadPool);
  }

  void sendChatHandshake(ChatHandshake chatHandshake) {
    twitchChatAdapter.sendChatHandshake(chatHandshake);
  }

  void setMessageDelay(Period delay) {
    twitchChatAdapter.setMessageDelay(delay);
  }
}
