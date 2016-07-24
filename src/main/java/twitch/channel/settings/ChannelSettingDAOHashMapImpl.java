package twitch.channel.settings;

import java.util.HashMap;
import java.util.Optional;

/**
 * Created by Dominic Hauton on 27/06/2016.
 *
 * Basic implementation of ChannelSettingDAO for local storage and testing.
 */
public class ChannelSettingDAOHashMapImpl implements ChannelSettingDAO {
    private final HashMap<String, Double> settingDoubleMap;

    public ChannelSettingDAOHashMapImpl() {
        settingDoubleMap = new HashMap<>();
    }

    @Override
    public Double getDoubleSetting(String channelName, ChannelSettingDouble channelSettingDouble) throws ChannelSettingDAOException {
        String hashMapKey = generateKey(channelName, channelSettingDouble);
        Optional<Double> mapResultOptional = Optional.of(settingDoubleMap.get(hashMapKey));
        return mapResultOptional.orElseThrow(
                () -> new ChannelSettingDAOException("Key " + hashMapKey + " not found."));
    }

    @Override
    public void setDoubleSetting(String channelName, ChannelSettingDouble channelSettingDouble, Double value) throws ChannelSettingDAOException {
        String hashMapKey = generateKey(channelName, channelSettingDouble);
        settingDoubleMap.put(hashMapKey, value);
    }

    private String generateKey(String channelName, ChannelSettingDouble channelSettingDouble) {
        return String.join("_", channelName, channelSettingDouble.toString());
    }
}
