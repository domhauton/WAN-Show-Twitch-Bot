package com.domhauton.wanbot.config;

import com.domhauton.wanbot.config.items.BitlyInfo;
import com.domhauton.wanbot.config.items.TwitchInfo;

import java.util.Objects;

/**
 * Created by dominic on 27/05/17.
 */
public class BotConfig {
  private TwitchInfo twitch = new TwitchInfo();
  private BitlyInfo bitly = new BitlyInfo();

  public BotConfig() {
  } // Jackson ONLY

  public BotConfig(TwitchInfo twitch, BitlyInfo bitly) {
    this.twitch = twitch;
    this.bitly = bitly;
  }

  public TwitchInfo getTwitch() {
    return twitch;
  }

  public BitlyInfo getBitly() {
    return bitly;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BotConfig botConfig = (BotConfig) o;
    return Objects.equals(getTwitch(), botConfig.getTwitch()) &&
        Objects.equals(getBitly(), botConfig.getBitly());
  }

  @Override
  public int hashCode() {

    return Objects.hash(getTwitch(), getBitly());
  }
}
