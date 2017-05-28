package twitch.chat.sender;

import config.IrcInfo;
import config.TwitchInfo;
import twitch.chat.data.OutboundTwitchMessage;
import twitch.chat.data.OutboundTwitchWhisper;
import twitch.chat.exceptions.TwitchChatException;

/**
 * Created by Dominic Hauton on 12/03/2016.
 * <p>
 * An router to control the sending of twitch chat messages.
 */
public class TwitchMessageRouter {
  private final MessageSender whisperSender;
  private final MessageSender channelSender;
  private final TwitchInfo twitchInfo;

  public TwitchMessageRouter(TwitchInfo twitchInfo) {
    this.twitchInfo = twitchInfo;
    this.whisperSender = new MessageSender(twitchInfo.getUsername(), twitchInfo.getoAuth());
    this.channelSender = new MessageSender(twitchInfo.getUsername(), twitchInfo.getoAuth());
  }

  public void sendMessage(OutboundTwitchMessage outboundTwitchMessage) {
    if (outboundTwitchMessage instanceof OutboundTwitchWhisper) {
      whisperSender.sendMessageAsync(outboundTwitchMessage);
    } else {
      channelSender.sendMessageAsync(outboundTwitchMessage);
    }
  }

  public void connect() throws TwitchChatException {
    IrcInfo whisperInfo = twitchInfo.getWhisper();
    whisperSender.connect(whisperInfo.getChannelName(), whisperInfo.getHostname(), whisperInfo.getPort());
    IrcInfo channelInfo = twitchInfo.getChannel();
    channelSender.connect(channelInfo.getChannelName(), channelInfo.getHostname(), channelInfo.getPort());
  }
}
