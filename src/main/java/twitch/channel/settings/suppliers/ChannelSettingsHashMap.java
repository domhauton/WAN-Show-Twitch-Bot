package twitch.channel.settings.suppliers;

import com.google.common.reflect.TypeToken;
import twitch.channel.settings.ChannelSettingSupplier;
import twitch.channel.settings.exceptions.DatabaseException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Optional;

/**
 * Created by Dominic Hauton on 27/06/2016.
 */
public class ChannelSettingsHashMap implements ChannelSettingSupplier {
    private final HashMap<String, Serializable> settingMap;

    public ChannelSettingsHashMap() {
        settingMap = new HashMap<>();
    }

    @Override

    public <T extends Serializable> T get(String channelName, String key, TypeToken<T> clazz) throws DatabaseException {
        String hashMapKey = generateKey(channelName, key);
        Optional<Serializable> mapResultOptional = Optional.of(settingMap.get(hashMapKey));
        Serializable mapResult = mapResultOptional.orElseThrow(
                () -> new DatabaseException("Key " + hashMapKey + " not found."));
        return checkedCast(mapResult, clazz);
    }

    @Override
    public <T extends Serializable> void set(String channelName, String key, T value, TypeToken<T> clazz) throws
            DatabaseException {
        String hashMapKey = generateKey(channelName, key);
        settingMap.put(hashMapKey, value);
    }

    private String generateKey(String channelName, String key) {
        return channelName + "_" + key;
    }

    @SuppressWarnings("unchecked") // No way to cast generics.
    private <T> T checkedCast(Serializable input, TypeToken<T> outputType) throws DatabaseException {

        if (outputType.isSupertypeOf(input.getClass())) {
            return (T) input;
        } else {
            throw new DatabaseException("Failed to cast to " + outputType.toString());
        }
    }
}
