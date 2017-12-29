package com.domhauton.wanbot.config;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by dominic on 27/05/17.
 */
public abstract class ConfigManager {
  private final static Logger logger = LogManager.getLogger();
  private final static ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

  public static void saveConfig(Path configPath, BotConfig config) throws ConfigException {
    try {
      logger.info("Wanbot Config - Saving config. \t\t[{}]", configPath);
      mapper.writeValue(configPath.toFile(), config);
    } catch (IOException e) {
      logger.error("Wanbot Config - Failed to open file. \t[{}]", configPath);
      logger.debug(e);
      throw new ConfigException("Failed to open file. " + e.getMessage());
    }
  }

  public static BotConfig loadConfig(Path filePath) throws ConfigException {
    try {
      logger.info("Wanbot Config - Loading config. \t[{}]", filePath);
      return mapper.readValue(filePath.toFile(), BotConfig.class);
    } catch (JsonParseException e) {
      logger.error("Wanbot Config - Failed to parse YAML. \t[{}]", filePath);
      logger.debug(e);
      throw new ConfigException("Failed to parse YAML. " + e.getMessage());
    } catch (JsonMappingException e) {
      logger.error("Wanbot Config - Config could not be parsed. \t[{}]", filePath);
      logger.debug(e);
      throw new ConfigException("Config has invalid fields. " + e.getMessage());
    } catch (IOException e) {
      logger.error("Wanbot Config - Failed to open file. \t[{}]", filePath);
      logger.debug(e);
      throw new ConfigException("Failed to open file. " + e.getMessage());
    }
  }

  public static BotConfig loadDefaultConfig() {
    return new BotConfig();
  }
}
