package config;

/**
 * Created by dominic on 27/05/17.
 */
public class IrcInfo {
  private String hostname;
  private int port;
  private String channel;
  private ChatRate rate;

  private IrcInfo() {
    // Jackson ONLY
  }

  public IrcInfo(String hostname, int port, String channel, ChatRate rate) {
    this.hostname = hostname;
    this.port = port;
    this.channel = channel;
    this.rate = rate;
  }

  public String getHostname() {
    return hostname;
  }

  public int getPort() {
    return port;
  }

  public String getChannelName() {
    return channel;
  }

  public ChatRate getRate() {
    return rate;
  }
}
