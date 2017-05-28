package config;

/**
 * Created by dominic on 27/05/17.
 */
public class BotConfig {
  private TwitchInfo twitch;
  private BitlyInfo bitly;

  public BotConfig() {
  } // Jackson ONLY

  public BotConfig(TwitchInfo twitch, BitlyInfo bitly) {
    this.twitch = twitch;
    this.bitly = bitly;
  }

  public TwitchInfo getTwitchInfo() {
    return twitch;
  }

  public BitlyInfo getBitly() {
    return bitly;
  }
}
