package util.config;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import irc.util.EventBuffer;
import irc.util.EventBufferFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.DateTimeUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Properties;

/**
 * Created by Dominic Hauton on 24/02/2016.
 *
 * DI for Guice
 */
public class AppInjector extends AbstractModule{

    private Environment environment;
    private Logger log = LogManager.getLogger();

    public AppInjector(Environment environment) {
        this.environment = environment;
    }

    @Override
    protected void configure() {
        Properties loadedProperties = loadProperties(environment.getConfigFileName());
        Names.bindProperties(binder(), loadedProperties);

        bind(DateTimeUtil.class).asEagerSingleton();
        install(new FactoryModuleBuilder()
                .build(EventBufferFactory.class));
    }

    private Properties loadProperties(String filename) {

        URL propertyResource = AppInjector.class.getClassLoader().getResource(filename);
        if(propertyResource == null) {
            log.fatal("Failed to find resource for properties file: {}", filename);
            System.exit(1);
        }
        String fullFileName = propertyResource.getFile();
        log.info("Loading log from {}", fullFileName);
        try( InputStream propertyInputStream = propertyResource.openStream() ) {
            Properties properties = new Properties();
            properties.load(propertyInputStream);
            log.info("Success loading: {}", fullFileName);
            return properties;
        } catch (FileNotFoundException e) {
            log.error("Missing properties file in: {}", fullFileName);
            throw new UncheckedIOException(e);
        } catch (IOException e) {
            log.error("IOException {} while opening properties file {}", e.getCause(), filename);
            throw new UncheckedIOException(e);
        }
    }
}
