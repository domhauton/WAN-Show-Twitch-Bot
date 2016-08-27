package bot.util.config;

/**
 * Created by Dominic Hauton on 16/03/2016.
 */
public enum Environment {
  PROD("bot.properties"),
  DEV("bot-dev.properties");

  private String configFileName;

  Environment(String configFileName) {
    this.configFileName = configFileName;
  }

  public String getConfigFileName() {
    return configFileName;
  }
}
