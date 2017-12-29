package com.domhauton.wanbot.config.items;

import java.util.Objects;

/**
 * Created by dominic on 27/05/17.
 */
public class TwitchIrc {
  private String hostname = "irc.chat.twitch.tv";
  private int port = 6667;
  private String channel;
  private RateLimit rateLimits;

  private TwitchIrc() {
    // Jackson ONLY
  }

  public TwitchIrc(String hostname, int port, String channel, RateLimit rateLimits) {
    this.hostname = hostname;
    this.port = port;
    this.channel = channel;
    this.rateLimits = rateLimits;
  }

  public String getHostname() {
    return hostname;
  }

  public int getPort() {
    return port;
  }

  public String getChannel() {
    return channel;
  }

  public RateLimit getRateLimits() {
    return rateLimits;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TwitchIrc twitchIrc = (TwitchIrc) o;
    return getPort() == twitchIrc.getPort() &&
        Objects.equals(getHostname(), twitchIrc.getHostname()) &&
        Objects.equals(getChannel(), twitchIrc.getChannel()) &&
        Objects.equals(getRateLimits(), twitchIrc.getRateLimits());
  }

  @Override
  public int hashCode() {

    return Objects.hash(getHostname(), getPort(), getChannel(), getRateLimits());
  }
}
