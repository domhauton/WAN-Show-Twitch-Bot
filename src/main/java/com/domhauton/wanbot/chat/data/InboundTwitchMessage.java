package com.domhauton.wanbot.chat.data;

/**
 * Created by Dominic Hauton on 12/04/2016.
 *
 * Data class containing an Inbound Twitch Message
 */
public class InboundTwitchMessage {
  private String twitchChannel;
  private String username;
  private String message;

  public InboundTwitchMessage(String twitchChannel, String username, String message) {
    this.twitchChannel = twitchChannel;
    this.username = username;
    this.message = message;
  }

  public String getTwitchChannel() {
    return twitchChannel;
  }

  public String getUsername() {
    return username;
  }

  public String getMessage() {
    return message;
  }
}
