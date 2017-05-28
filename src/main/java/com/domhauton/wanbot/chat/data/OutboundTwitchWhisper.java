package com.domhauton.wanbot.chat.data;

/**
 * Created by Dominic Hauton on 09/07/2016.
 *
 * Whisper MESSAGE builder with no target user.
 */
public class OutboundTwitchWhisper extends OutboundTwitchMessage {

  public OutboundTwitchWhisper(String payload, String targetUser) {
    super(buildWhisper(payload, targetUser), "");
  }

  private static String buildWhisper(String message, String targetUser) {
    return ".w " + targetUser + message;
  }
}
