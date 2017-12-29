package com.domhauton.wanbot.chat.sender;

import com.domhauton.wanbot.chat.data.OutboundTwitchMessage;
import com.domhauton.wanbot.chat.data.OutboundTwitchWhisper;
import com.domhauton.wanbot.chat.exceptions.TwitchChatException;
import com.domhauton.wanbot.config.items.TwitchInfo;
import com.domhauton.wanbot.config.items.TwitchIrc;

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
    this.whisperSender = new MessageSender("foo1", "bar1");
    this.channelSender = new MessageSender("foo2", "bar2");
  }

  public void sendMessage(OutboundTwitchMessage outboundTwitchMessage) {
    if (outboundTwitchMessage instanceof OutboundTwitchWhisper) {
      whisperSender.sendMessageAsync(outboundTwitchMessage);
    } else {
      channelSender.sendMessageAsync(outboundTwitchMessage);
    }
  }

  public void connect() throws TwitchChatException {
    TwitchIrc whisperInfo = twitchInfo.getWhisper();
    whisperSender.connect(whisperInfo.getChannel(), whisperInfo.getHostname(), whisperInfo.getPort());
    TwitchIrc channelInfo = twitchInfo.getChannel();
    channelSender.connect(channelInfo.getChannel(), channelInfo.getHostname(), channelInfo.getPort());
  }
}
