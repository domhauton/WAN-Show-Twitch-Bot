package com.domhauton.wanbot.irc;

/**
 * Created by Dominic Hauton on 12/04/2016.
 *
 * Data class for IRC Messages
 */
final class InboundIRCMessage {
  private String channel;
  private String sender;
  private String login;
  private String hostname;
  private String message;

  InboundIRCMessage(String channel, String sender, String login, String hostname, String message) {
    this.channel = channel;
    this.sender = sender;
    this.login = login;
    this.hostname = hostname;
    this.message = message;
  }

  public String getChannel() {
    return channel;
  }

  public String getSender() {
    return sender;
  }

  @SuppressWarnings("unused") // Used as part of PircBot
  public String getLogin() {
    return login;
  }

  @SuppressWarnings("unused") // Used as part of PircBot
  public String getHostname() {
    return hostname;
  }

  public String getMessage() {
    return message;
  }
}
