package bot;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bot.util.config.AppInjector;
import bot.util.config.Environment;
import twitch.chat.listener.TwitchChannelListener;

public final class Start {
  private static final Logger log = LogManager.getLogger();

  public static void main(String[] args) {
    log.info("Starting Bot");
    Injector injector = Guice.createInjector(new AppInjector(Environment.PROD));
    BotController bot = injector.getInstance(BotController.class);
    TwitchChannelListener twitchChannelListener = injector.getInstance(TwitchChannelListener.class);
    twitchChannelListener.listen();
    twitchChannelListener.addOutput(bot::processMessage);
  }

}
