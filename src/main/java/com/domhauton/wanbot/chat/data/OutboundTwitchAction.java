package com.domhauton.wanbot.chat.data;

/**
 * Created by Dominic Hauton on 10/07/2016.
 *
 * Wraps an outbound twitch action.
 */
public class OutboundTwitchAction extends OutboundTwitchMessage {

  public OutboundTwitchAction(String payload, String targetChannel) {
    super(buildAction(payload), targetChannel);
  }

  private static String buildAction(String message) {
    return ".me " + message;
  }
}



