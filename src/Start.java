import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Start {

	private static final String channel = "#linustech";


	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) {
		log.info("Starting Bot");
		Injector injector = Guice.createInjector(new AppInjector());
		MyBot bot = injector.getInstance( MyBot.class );
	}

}
