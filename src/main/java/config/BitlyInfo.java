package config;

/**
 * Created by dominic on 27/05/17.
 */
public class BitlyInfo {
  private String username;
  private String oauth;

  private BitlyInfo() {
    // Jackson ONLY
  }

  public BitlyInfo(String username, String oauth) {
    this.username = username;
    this.oauth = oauth;
  }
}
