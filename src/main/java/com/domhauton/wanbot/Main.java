package com.domhauton.wanbot;

import com.domhauton.wanbot.bot.BotController;
import com.domhauton.wanbot.chat.listener.TwitchChannelListener;
import com.domhauton.wanbot.chat.sender.TwitchMessageRouter;
import com.domhauton.wanbot.config.BotConfig;
import com.domhauton.wanbot.config.ConfigLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class Main {
  private static final Logger LOGGER = LogManager.getLogger();

  public static void main(String[] args) {
    LOGGER.info("Starting Bot");

    ConfigLoader configLoader = new ConfigLoader(getConfigDir());
    BotConfig botConfig = configLoader.load();

    TwitchMessageRouter twitchMessageRouter = new TwitchMessageRouter(botConfig.getTwitchInfo());

    BotController bot = new BotController(twitchMessageRouter::sendMessage, botConfig.getTwitchInfo().getChannel().getChannelName());

    TwitchChannelListener twitchChannelListener = new TwitchChannelListener(botConfig.getTwitchInfo());
    twitchChannelListener.listen();
    twitchChannelListener.addOutput(bot::processMessage);
  }

  static Path getConfigDir() {
    return Paths.get(System.getProperty("user.home"));
  }

}
