package com.domhauton.wanbot.config.items;

import java.util.Objects;

/**
 * Created by dominic on 27/05/17.
 */
public class TwitchInfo {
  private TwitchIrc channel = new TwitchIrc("irc.chat.twitch.tv", 6667, "#linustech", new RateLimit(30, 80));
  private TwitchIrc whisper = new TwitchIrc("irc.chat.twitch.tv", 6667, "#linustech", new RateLimit(60, 99));

  public TwitchInfo() {
    // JACKSON ONLY
  }

  public TwitchInfo(TwitchIrc channel, TwitchIrc whisper) {
    this.channel = channel;
    this.whisper = whisper;
  }

  public TwitchIrc getChannel() {
    return channel;
  }

  public TwitchIrc getWhisper() {
    return whisper;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TwitchInfo that = (TwitchInfo) o;
    return Objects.equals(getChannel(), that.getChannel()) &&
        Objects.equals(getWhisper(), that.getWhisper());
  }

  @Override
  public int hashCode() {

    return Objects.hash(getChannel(), getWhisper());
  }
}
