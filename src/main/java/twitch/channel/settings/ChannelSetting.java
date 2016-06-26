package twitch.channel.settings;

import com.google.common.reflect.TypeToken;
import twitch.channel.settings.exceptions.DatabaseException;

import java.io.Serializable;

/**
 * Created by Dominic Hauton on 26/06/2016.
 *
 * Channel Setting that handles retrieval and updating of setting value
 */
public class ChannelSetting<T extends Serializable> {

    private final T defaultValue;
    private final String key;
    private final ChannelSettingSupplier channelSettingSupplier;
    private final TypeToken<T> typeToken = new TypeToken<T>() {}; // Used due to Type Erasure

    ChannelSetting(T defaultValue, String key, ChannelSettingSupplier channelSettingSupplier) {
        this.channelSettingSupplier = channelSettingSupplier;
        this.defaultValue = defaultValue;
        this.key = key;
    }

    private T get() throws DatabaseException{
        return channelSettingSupplier.get(key, typeToken);
    }

    private T getOrDefault() throws DatabaseException{
        try{
            return get();
        } catch (DatabaseException e){
            return defaultValue;
        }
    }

    private T set(T value) throws DatabaseException{
        return channelSettingSupplier.set(key, value, typeToken);
    }
}
