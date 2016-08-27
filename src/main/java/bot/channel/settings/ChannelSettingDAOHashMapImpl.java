package bot.channel.settings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import bot.channel.settings.enums.IChannelSetting;

import java.util.HashMap;
import java.util.Optional;

/**
 * Created by Dominic Hauton on 27/06/2016.
 *
 * Basic implementation of ChannelSettingDao for local storage and testing.
 */
public class ChannelSettingDAOHashMapImpl implements ChannelSettingDao {
    private final HashMap<String, Object> settingMap;
    private static final Logger s_log = LogManager.getLogger();

    public ChannelSettingDAOHashMapImpl() {
        this(new HashMap<>());
    }

    ChannelSettingDAOHashMapImpl(HashMap<String, Object> settingMap) {
        this.settingMap = settingMap;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getSetting(String channelName, IChannelSetting<T> channelSetting) throws ChannelSettingDAOException{
        s_log.info("Retrieving setting {} for channel {}", channelSetting, channelName);
        String hashMapKey = generateKey(channelName, channelSetting);
        Optional<Object> mapResultOptional = Optional.ofNullable(settingMap.get(hashMapKey));
        Object mapResult = mapResultOptional.orElseThrow(
                () -> new ChannelSettingDAOException("Key " + hashMapKey + " not found."));
        if( channelSetting.getGenericInterfaceType().isInstance(mapResult) ){
            try {
                return (T) mapResult;
            } catch (ClassCastException e) {
                settingMap.remove(hashMapKey);
                throw new ChannelSettingDAOException("Cast fail on entry. Removed from table for safety. Try again.");
            }
        } else {
            s_log.error("Database corrupted. Setting of incorrect type present for {}", channelSetting);
            throw new ChannelSettingDAOException("Database returned object of incorrect type!");
        }
    }

    @Override
    public <T> void setSetting(String channelName, IChannelSetting<T> channelSetting, T value) throws ChannelSettingDAOException{
        String hashMapKey = generateKey(channelName, channelSetting);
        settingMap.put(hashMapKey, value);
    }

    static <T> String generateKey(String channelName, IChannelSetting<T> channelSetting) {
        return String.join("#", channelName, channelSetting.getGenericInterfaceType().getSimpleName(), channelSetting
                .toString());
    }
}
