package com.domhauton.wanbot.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.*;
import java.security.SecureRandom;


/**
 * Created by dominic on 23/01/17.
 */
class ConfigManagerTest {
  private static final SecureRandom secureRandom = new SecureRandom();
  private String testCfgLocation;

  @BeforeEach
  void setUp() throws Exception {
    String testCfgDirLocation = createRandomFolder(System.getProperty("java.io.tmpdir") + File.separator + "wanbot-testing");
    testCfgLocation = testCfgDirLocation + File.separator + "wanbot.yml";
  }

  @Test
  @DisplayName("Saving works")
  void saveConfig() throws Exception {
    Files.deleteIfExists(Paths.get(testCfgLocation));
    Assertions.assertFalse(Files.deleteIfExists(Paths.get(testCfgLocation)));

    ConfigManager.saveConfig(Paths.get(testCfgLocation), ConfigManager.loadDefaultConfig());
    Assertions.assertTrue(Files.deleteIfExists(Paths.get(testCfgLocation)));
    Assertions.assertFalse(Files.deleteIfExists(Paths.get(testCfgLocation)));
  }

  @Test
  @DisplayName("Saving IO Exception works")
  void saveConfigFails() throws Exception {
    Files.deleteIfExists(Paths.get(testCfgLocation));
    Assertions.assertFalse(Files.deleteIfExists(Paths.get(testCfgLocation)));

    ConfigManager.saveConfig(Paths.get(testCfgLocation), ConfigManager.loadDefaultConfig());
    Assertions.assertTrue(Paths.get(testCfgLocation).toFile().setWritable(false));
    Assertions.assertThrows(ConfigException.class, () -> ConfigManager.saveConfig(Paths.get(testCfgLocation), ConfigManager.loadDefaultConfig()));
    Assertions.assertTrue(Paths.get(testCfgLocation).toFile().setWritable(true));
    Assertions.assertTrue(Files.deleteIfExists(Paths.get(testCfgLocation)));
    Assertions.assertFalse(Files.deleteIfExists(Paths.get(testCfgLocation)));
  }

  @Test
  @DisplayName("Loading IO Exception works")
  void loadConfigFails() throws Exception {
    Files.deleteIfExists(Paths.get(testCfgLocation));
    Assertions.assertFalse(Files.deleteIfExists(Paths.get(testCfgLocation)));

    ConfigManager.saveConfig(Paths.get(testCfgLocation), ConfigManager.loadDefaultConfig());
    Assertions.assertTrue(Paths.get(testCfgLocation).toFile().setReadable(false));
    Assertions.assertThrows(ConfigException.class, () -> ConfigManager.loadConfig(Paths.get(testCfgLocation)));
    Assertions.assertTrue(Paths.get(testCfgLocation).toFile().setReadable(true));

    Assertions.assertEquals(ConfigManager.loadDefaultConfig(), ConfigManager.loadConfig(Paths.get(testCfgLocation)));

    Assertions.assertTrue(Files.deleteIfExists(Paths.get(testCfgLocation)));
    Assertions.assertFalse(Files.deleteIfExists(Paths.get(testCfgLocation)));
  }

  @Test
  @DisplayName("Loading invalid YAML works")
  void loadConfigFailsYAML() throws Exception {
    Files.deleteIfExists(Paths.get(testCfgLocation));
    Assertions.assertFalse(Files.deleteIfExists(Paths.get(testCfgLocation)));

    ConfigManager.saveConfig(Paths.get(testCfgLocation), ConfigManager.loadDefaultConfig());

    Files.write(Paths.get(testCfgLocation), "tsdatdra".getBytes(), StandardOpenOption.APPEND);

    Assertions.assertThrows(ConfigException.class, () -> ConfigManager.loadConfig(Paths.get(testCfgLocation)));

    Assertions.assertTrue(Files.deleteIfExists(Paths.get(testCfgLocation)));
    Assertions.assertFalse(Files.deleteIfExists(Paths.get(testCfgLocation)));
  }

  @Test
  @DisplayName("Loading from a save")
  void saveLoadTest() throws Exception {
    Files.deleteIfExists(Paths.get(testCfgLocation));
    Assertions.assertFalse(Files.deleteIfExists(Paths.get(testCfgLocation)));

    BotConfig cfg = ConfigManager.loadDefaultConfig();
    boolean defaultVal = cfg.getBitly().isEnabled();
    cfg.getBitly().setEnabled(!defaultVal);

    ConfigManager.saveConfig(Paths.get(testCfgLocation), cfg);
    BotConfig config = ConfigManager.loadConfig(Paths.get(testCfgLocation));
    Assertions.assertEquals(!defaultVal, config.getBitly().isEnabled());
    Assertions.assertEquals(defaultVal, ConfigManager.loadDefaultConfig().getBitly().isEnabled());
  }

  @Test
  @DisplayName("Loading from an overwritten save")
  void overwriteTest() throws Exception {
    Files.deleteIfExists(Paths.get(testCfgLocation));
    Assertions.assertFalse(Files.deleteIfExists(Paths.get(testCfgLocation)));

    BotConfig cfg = ConfigManager.loadDefaultConfig();
    String defaultChannel = cfg.getTwitch().getChannel().getChannel();
    cfg.getTwitch().getChannel().setChannel("#foobar1");
    ConfigManager.saveConfig(Paths.get(testCfgLocation), cfg);

    BotConfig loadedConfig1 = ConfigManager.loadConfig(Paths.get(testCfgLocation));
    Assertions.assertEquals("#foobar1", loadedConfig1.getTwitch().getChannel().getChannel());
    Assertions.assertEquals(defaultChannel, ConfigManager.loadDefaultConfig().getTwitch().getChannel().getChannel());
    loadedConfig1.getTwitch().getChannel().setChannel("#foobar2");
    ConfigManager.saveConfig(Paths.get(testCfgLocation), cfg);

    BotConfig loadedConfig2 = ConfigManager.loadConfig(Paths.get(testCfgLocation));
    Assertions.assertEquals("#foobar1", loadedConfig2.getTwitch().getChannel().getChannel());
  }

  public static String createRandomFolder(String baseDir) throws Exception {
    String tmpDir = baseDir;
    Path tmpPath = Paths.get(baseDir);
    Files.createDirectories(tmpPath);
    while (Files.exists(tmpPath, LinkOption.NOFOLLOW_LINKS)) {
      tmpDir = baseDir + File.separator + new BigInteger(16, secureRandom).toString(32);
      tmpPath = Paths.get(tmpDir);
    }
    Files.createDirectories(tmpPath);
    return tmpDir;
  }
}