package irc;

/**
 * Created by Dominic Hauton on 13/04/2016.
 *
 * Contains a list of all the used handshakes
 */
public enum ChatHandshake {
  COMMANDS("CAP REQ :twitch.tv/commands"),
  MEMBERSHIP("CAP REQ :twitch.tv/membership");

  private String handshake;

  ChatHandshake(String handshake) {
    this.handshake = handshake;
  }

  public String getHandshake() {
    return handshake;
  }
}
