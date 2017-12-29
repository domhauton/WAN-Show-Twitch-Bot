package com.domhauton.wanbot;

import com.domhauton.wanbot.bot.BotController;
import com.domhauton.wanbot.chat.listener.TwitchChannelListener;
import com.domhauton.wanbot.chat.sender.TwitchMessageRouter;
import com.domhauton.wanbot.config.BotConfig;
import com.domhauton.wanbot.config.ConfigException;
import com.domhauton.wanbot.config.ConfigManager;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Main {
  private static final Logger LOGGER = LogManager.getLogger();
  private static final String DEFAULT_CONFIG_LOCATION = System.getProperty("user.home") + File.separator + "wanbot" + File.separator + "config.yml";

  public static void main(String[] args) {
    LOGGER.info("Starting Bot");
    Options options = new Options()
        .addOption("c", "config", true, "Specify the config directory. Default: [" + DEFAULT_CONFIG_LOCATION + "]");

    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine cmd = parser.parse(options, args);
      Path configLocation = Paths.get(cmd.getOptionValue("config", DEFAULT_CONFIG_LOCATION));
      BotConfig botConfig = getBotConfig(configLocation);
      startBot(botConfig);
    } catch (ParseException e) {
      LOGGER.error("Argument parsing failed. Reason: {}", e.getMessage());
      System.exit(1);
    } catch (ConfigException e) {
      LOGGER.error("Config loading failed. Reason: {}", e.getMessage());
      System.exit(2);
    }
  }

  private static BotConfig getBotConfig(Path configLocation) throws ConfigException {
    BotConfig botConfig;
    try {
      return ConfigManager.loadConfig(configLocation);
    } catch (ConfigException e) {
      botConfig = ConfigManager.loadDefaultConfig();
      ConfigManager.saveConfig(configLocation, botConfig);
      return botConfig;
    }
  }

  private static void startBot(BotConfig botConfig) {
    TwitchMessageRouter twitchMessageRouter = new TwitchMessageRouter(botConfig.getTwitch());

    BotController bot = new BotController(twitchMessageRouter::sendMessage, botConfig.getTwitch().getChannel().getChannel());

    TwitchChannelListener twitchChannelListener = new TwitchChannelListener(botConfig.getTwitch());
    twitchChannelListener.listen();
    twitchChannelListener.addOutput(bot::processMessage);
  }
}
