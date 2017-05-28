package config;

import java.nio.file.Path;

/**
 * Created by dominic on 27/05/17.
 */
public class ConfigLoader {
  private Path path;

  public ConfigLoader(Path path) {
    this.path = path;
  }

  public BotConfig load() {
    return load(path);
  }

  public void save(BotConfig botConfig) {
    save(botConfig, path);
  }

  BotConfig load(Path path) {
    return null;
  }

  void save(BotConfig botConfig, Path path) {

  }

}
