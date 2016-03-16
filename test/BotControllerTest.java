import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import util.config.AppInjector;
import util.config.Environment;

/**
 * Created by Dominic Hauton on 11/03/2016.
 */
public class BotControllerTest {

    private BotController botController;

    @Before
    public void setUp(){
        Injector injector = Guice.createInjector(new AppInjector(Environment.DEV));
        botController = injector.getInstance(BotController.class);
    }
}
