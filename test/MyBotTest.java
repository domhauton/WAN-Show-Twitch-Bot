import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Dominic Hauton on 11/03/2016.
 */
public class MyBotTest {

    private MyBot myBot;

    @Before
    public void setUp(){
        Injector injector = Guice.createInjector(new AppInjectorTest());
        myBot = injector.getInstance(MyBot.class);
    }

    @Test
    public void timeTillLiveTest(){
        System.out.println( myBot.getTimeTillLive() );
    }
}
