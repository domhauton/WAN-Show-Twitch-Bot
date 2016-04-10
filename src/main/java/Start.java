import com.google.inject.Guice;
import com.google.inject.Injector;
import twitchchat.TwitchChannelListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.config.AppInjector;
import util.config.Environment;

public class Start {
	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) {
		log.info("Starting Bot");
		Injector injector = Guice.createInjector(new AppInjector(Environment.PROD));
		BotController bot = injector.getInstance( BotController.class );
		TwitchChannelListener twitchChannelListener = injector.getInstance( TwitchChannelListener.class );
		twitchChannelListener.listen();
		twitchChannelListener.addOutput(bot::processMessage);
	}

}
