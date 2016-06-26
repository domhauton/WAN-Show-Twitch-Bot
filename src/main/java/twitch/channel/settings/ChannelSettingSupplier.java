package twitch.channel.settings;

import com.google.common.reflect.TypeToken;
import twitch.channel.settings.exceptions.DatabaseException;

import java.io.Serializable;

/**
 * Created by Dominic Hauton on 05/06/2016.
 *
 * An interface to be implemented by all Persistence layers
 */
public interface ChannelSettingSupplier {
    <T extends Serializable> T get(String key, TypeToken<T> clazz) throws DatabaseException;

    <T extends Serializable> T set(String key, T value, TypeToken<T> clazz) throws DatabaseException;
}
