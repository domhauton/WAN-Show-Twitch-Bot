package com.domhauton.wanbot.config;

import org.junit.jupiter.api.*;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.*;
import java.security.SecureRandom;


/**
 * Created by dominic on 23/01/17.
 */
class ConfigManagerTest {
  private static final SecureRandom secureRandom = new SecureRandom();
  private Path configFilePath;

  @BeforeEach
  void setUp() throws Exception {
    String testCfgDirLocation = createRandomFolder(System.getProperty("java.io.tmpdir") + File.separator + "wanbot-testing");
    String testCfgLocation = testCfgDirLocation + File.separator + "wanbot.yml";
    configFilePath = Paths.get(testCfgLocation);
  }

  @Test
  @DisplayName("Saving works")
  void saveConfig() throws Exception {
    Files.deleteIfExists(configFilePath);
    Assertions.assertFalse(Files.deleteIfExists(configFilePath));

    ConfigManager.saveConfig(configFilePath, ConfigManager.loadDefaultConfig());
    Assertions.assertTrue(Files.deleteIfExists(configFilePath));
    Assertions.assertFalse(Files.deleteIfExists(configFilePath));
  }

  @Test
  @Tag("dockerbuildskip")
  @DisplayName("Saving IO Exception works")
  void saveConfigFails() throws Exception {
    Files.deleteIfExists(configFilePath);
    Assertions.assertFalse(Files.deleteIfExists(configFilePath));

    ConfigManager.saveConfig(configFilePath, ConfigManager.loadDefaultConfig());
    Assertions.assertTrue(configFilePath.toFile().setWritable(false));
    Assertions.assertThrows(ConfigException.class, () -> ConfigManager.saveConfig(configFilePath, ConfigManager.loadDefaultConfig()));
    Assertions.assertTrue(configFilePath.toFile().setWritable(true));
    Assertions.assertTrue(Files.deleteIfExists(configFilePath));
    Assertions.assertFalse(Files.deleteIfExists(configFilePath));
  }

  @Test
  @Tag("dockerbuildskip")
  @DisplayName("Loading IO Exception works")
  void loadConfigFails() throws Exception {
    Files.deleteIfExists(configFilePath);
    Assertions.assertFalse(Files.deleteIfExists(configFilePath));

    ConfigManager.saveConfig(configFilePath, ConfigManager.loadDefaultConfig());
    Assertions.assertTrue(configFilePath.toFile().setReadable(false));
    Assertions.assertThrows(ConfigException.class, () -> ConfigManager.loadConfig(configFilePath));
    Assertions.assertTrue(configFilePath.toFile().setReadable(true));

    Assertions.assertEquals(ConfigManager.loadDefaultConfig(), ConfigManager.loadConfig(configFilePath));

    Assertions.assertTrue(Files.deleteIfExists(configFilePath));
    Assertions.assertFalse(Files.deleteIfExists(configFilePath));
  }

  @Test
  @DisplayName("Loading invalid YAML works")
  void loadConfigFailsYAML() throws Exception {
    Files.deleteIfExists(configFilePath);
    Assertions.assertFalse(Files.deleteIfExists(configFilePath));

    ConfigManager.saveConfig(configFilePath, ConfigManager.loadDefaultConfig());

    Files.write(configFilePath, "tsdatdra".getBytes(), StandardOpenOption.APPEND);

    Assertions.assertThrows(ConfigException.class, () -> ConfigManager.loadConfig(configFilePath));

    Assertions.assertTrue(Files.deleteIfExists(configFilePath));
    Assertions.assertFalse(Files.deleteIfExists(configFilePath));
  }

  @Test
  @DisplayName("Loading from a save")
  void saveLoadTest() throws Exception {
    Files.deleteIfExists(configFilePath);
    Assertions.assertFalse(Files.deleteIfExists(configFilePath));

    BotConfig cfg = ConfigManager.loadDefaultConfig();
    boolean defaultVal = cfg.getBitly().isEnabled();
    cfg.getBitly().setEnabled(!defaultVal);

    ConfigManager.saveConfig(configFilePath, cfg);
    BotConfig config = ConfigManager.loadConfig(configFilePath);
    Assertions.assertEquals(!defaultVal, config.getBitly().isEnabled());
    Assertions.assertEquals(defaultVal, ConfigManager.loadDefaultConfig().getBitly().isEnabled());
  }

  @Test
  @DisplayName("Loading from an overwritten save")
  void overwriteTest() throws Exception {
    Files.deleteIfExists(configFilePath);
    Assertions.assertFalse(Files.deleteIfExists(configFilePath));

    BotConfig cfg = ConfigManager.loadDefaultConfig();
    String defaultChannel = cfg.getTwitch().getChannel().getChannel();
    cfg.getTwitch().getChannel().setChannel("#foobar1");
    ConfigManager.saveConfig(configFilePath, cfg);

    BotConfig loadedConfig1 = ConfigManager.loadConfig(configFilePath);
    Assertions.assertEquals("#foobar1", loadedConfig1.getTwitch().getChannel().getChannel());
    Assertions.assertEquals(defaultChannel, ConfigManager.loadDefaultConfig().getTwitch().getChannel().getChannel());
    loadedConfig1.getTwitch().getChannel().setChannel("#foobar2");
    ConfigManager.saveConfig(configFilePath, cfg);

    BotConfig loadedConfig2 = ConfigManager.loadConfig(configFilePath);
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