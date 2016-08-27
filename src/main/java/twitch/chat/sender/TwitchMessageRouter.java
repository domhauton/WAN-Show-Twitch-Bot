package twitch.chat.sender;

import com.google.inject.Inject;

import javax.inject.Singleton;

import twitch.chat.data.OutboundTwitchMessage;
import twitch.chat.data.OutboundTwitchWhisper;
import twitch.chat.exceptions.TwitchChatException;

/**
 * Created by Dominic Hauton on 12/03/2016.
 *
 * An router to control the sending of twitch chat messages.
 */
@Singleton
public class TwitchMessageRouter {
  private WhisperSender whisperSender;
  private ChannelSenderPool channelSenderPool;

  @Inject
  public TwitchMessageRouter(
      WhisperSender whisperSender,
      ChannelSenderPool channelSenderPool) {
    this.whisperSender = whisperSender;
    try {
      whisperSender.connect();
    } catch (TwitchChatException e) {
      // FIXME: 16/04/2016
      System.err.println("Could not connect to whisper chat");
    }
    this.channelSenderPool = channelSenderPool;
  }

  public void sendMessage(OutboundTwitchMessage outboundTwitchMessage) {
    if (outboundTwitchMessage instanceof OutboundTwitchWhisper) {
      whisperSender.sendMessageAsync(outboundTwitchMessage);
    } else {
      channelSenderPool.sendChannelMessage(outboundTwitchMessage);
    }
  }
}
