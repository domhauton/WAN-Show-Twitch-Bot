package twitch.chat.sender;

import javax.inject.Inject;

import twitch.chat.data.OutboundTwitchMessage;
import twitch.chat.exceptions.TwitchChatException;

/**
 * Created by Dominic Hauton on 16/03/2016.
 *
 * Will manage PublicMessageSender instances to allow constant MESSAGE sending.
 */
class ChannelSenderPool {
  private ChannelSender channelSender;

  @Inject
  ChannelSenderPool(ChannelSender channelSender) {
    this.channelSender = channelSender;
    try {
      channelSender.connect();
    } catch (TwitchChatException e) {
      //TODO FIXME
      System.err.println("Could not connect");
    }
  }

  void sendChannelMessage(OutboundTwitchMessage outboundTwitchMessage) {
    channelSender.sendChannelMessage(outboundTwitchMessage);
  }
}
