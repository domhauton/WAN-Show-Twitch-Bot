import bot.BotController;
import config.BotConfig;
import config.ConfigLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitch.chat.listener.TwitchChannelListener;
import twitch.chat.sender.TwitchMessageRouter;

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
