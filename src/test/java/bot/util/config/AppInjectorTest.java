package bot.util.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.UncheckedIOException;

/**
 * Created by Dominic Hauton on 27/08/2016.
 *
 * Tests for app property injector
 */
public class AppInjectorTest {

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  @Test
  public void loadFromFileSimpleProdTest() throws Exception {
    AppInjector appInjector = new AppInjector(null);
    appInjector.loadProperties(Environment.PROD.getConfigFileName());
  }

  @Test
  public void loadFromFileSimpleDevTest() throws Exception {
    AppInjector appInjector = new AppInjector(null);
    appInjector.loadProperties(Environment.DEV.getConfigFileName());
  }

  @Test
  public void loadFromFileLoadFailTest() throws Exception {
    AppInjector appInjector = new AppInjector(Environment.PROD);
    expectedException.expect(UncheckedIOException.class);
    appInjector.loadProperties("thisfiledoesnotexist.txt");
  }
}