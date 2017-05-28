package com.domhauton.wanbot.config;

/**
 * Created by dominic on 27/05/17.
 */
public class TwitchInfo {
  private String username;
  private String oAuth;
  private IrcInfo channel;
  private IrcInfo whisper;

  private TwitchInfo() {
    // Jackson ONLY
  }

  public TwitchInfo(String username, String oAuth, IrcInfo channel, IrcInfo whisper) {
    this.username = username;
    this.oAuth = oAuth;
    this.channel = channel;
    this.whisper = whisper;
  }

  public String getUsername() {
    return username;
  }

  public String getoAuth() {
    return oAuth;
  }

  public IrcInfo getChannel() {
    return channel;
  }

  public IrcInfo getWhisper() {
    return whisper;
  }
}
