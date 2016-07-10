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
    private final String channelName;
    private final ChannelSettingSupplier channelSettingSupplier;
    private final TypeToken<T> typeToken = new TypeToken<T>() {}; // Used due to Type Erasure

    ChannelSetting(T defaultValue, String channelName, String key, ChannelSettingSupplier channelSettingSupplier) {
        this.channelSettingSupplier = channelSettingSupplier;
        this.channelName = channelName;
        this.defaultValue = defaultValue;
        this.key = key;
    }

    public T get() throws DatabaseException{
        return channelSettingSupplier.get(channelName, key, typeToken);
    }

    public T getOrDefault(){
        try{
            return get();
        } catch (DatabaseException e){
            return defaultValue;
        }
    }

    public void set(T value) throws DatabaseException{
        channelSettingSupplier.set(channelName, key, value, typeToken);
    }
}
