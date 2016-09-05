package bot.channel.url;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.rosaloves.bitlyj.Bitly;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Named;

/**
 * Created by Dominic Hauton on 23/02/2016.
 *
 * Decorator to allow dependency injection
 */
@Singleton
public class BitlyDecorator {
  private static final Logger log = LogManager.getLogger();

  private Bitly.Provider bitlyProvider;

  @Inject
  public BitlyDecorator(
      @Named("bitly.username") String bitlyUsername,
      @Named("bitly.token") String bitlyToken) {
    log.info("Logging into bitly as user {}", bitlyUsername);
    bitlyProvider = Bitly.as(bitlyUsername, bitlyToken);
  }

  public String shortenURL(String longURL) {
    return bitlyProvider.call(Bitly.shorten(longURL)).getShortUrl();
  }
}
