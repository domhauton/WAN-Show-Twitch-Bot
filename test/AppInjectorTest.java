import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Created by Dominic Hauton on 24/02/2016.
 *
 * DI for Guice
 */
public class AppInjectorTest extends AbstractModule{

    private static final String propertiesFileName = "bot.properties";
    private static final Logger log = LogManager.getLogger();

    @Override
    protected void configure() {
        URL propertyResource = AppInjectorTest.class.getResource(propertiesFileName);
        if(propertyResource == null) {
            log.fatal("Failed to load properties file: {}", propertiesFileName);
            System.exit(1);
        }
        String fullFileName = propertyResource.getFile();
        log.info("Loading log from {}", fullFileName);
        try( InputStream propertyInputStream = propertyResource.openStream() ) {
            Properties properties = new Properties();
            properties.load(propertyInputStream);
            Names.bindProperties(binder(), properties);
        } catch (FileNotFoundException e) {
            log.error("Missing properties file in: {}", fullFileName);
            System.exit(1);
        } catch (IOException e) {
            log.error("IOException {} while opening properties file {}", e.getCause(), propertiesFileName);
            System.exit(1);
        }
        log.info("Success loading: {}", fullFileName);
    }
}
