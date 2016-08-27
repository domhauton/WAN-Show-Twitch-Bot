package bot.channel.settings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Optional;

import bot.channel.settings.enums.IChannelSetting;

/**
 * Created by Dominic Hauton on 27/06/2016.
 *
 * Basic implementation of ChannelSettingDao for local storage and testing.
 */
public class ChannelSettingDAOHashMapImpl implements ChannelSettingDao {
  private static final Logger log = LogManager.getLogger();
  private final HashMap<String, Object> settingMap;

  public ChannelSettingDAOHashMapImpl() {
    this(new HashMap<>());
  }

  ChannelSettingDAOHashMapImpl(HashMap<String, Object> settingMap) {
    this.settingMap = settingMap;
  }

  static <T> String generateKey(String channelName, IChannelSetting<T> channelSetting) {
    return String.join("#", channelName, channelSetting.getGenericInterfaceType().getSimpleName(), channelSetting
        .toString());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getSetting(String channelName, IChannelSetting<T> channelSetting) throws ChannelSettingDAOException {
    log.info("Retrieving setting {} for channel {}", channelSetting, channelName);
    String hashMapKey = generateKey(channelName, channelSetting);
    Optional<Object> mapResultOptional = Optional.ofNullable(settingMap.get(hashMapKey));
    Object mapResult = mapResultOptional.orElseThrow(
        () -> new ChannelSettingDAOException("Key " + hashMapKey + " not found."));
    if (channelSetting.getGenericInterfaceType().isInstance(mapResult)) {
      try {
        return (T) mapResult;
      } catch (ClassCastException e) {
        settingMap.remove(hashMapKey);
        throw new ChannelSettingDAOException("Cast fail on entry. Removed from table for safety. Try again.");
      }
    } else {
      log.error("Database corrupted. Setting of incorrect type present for {}", channelSetting);
      throw new ChannelSettingDAOException("Database returned object of incorrect type!");
    }
  }

  @Override
  public <T> void setSetting(String channelName, IChannelSetting<T> channelSetting, T value) throws ChannelSettingDAOException {
    String hashMapKey = generateKey(channelName, channelSetting);
    settingMap.put(hashMapKey, value);
  }
}
