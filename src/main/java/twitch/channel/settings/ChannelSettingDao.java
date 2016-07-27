package twitch.channel.settings;

import twitch.channel.settings.enums.IChannelSetting;

/**
 * Created by Dominic Hauton on 05/06/2016.
 * <p>
 * An interface to be implemented by all Persistence layers
 */
public interface ChannelSettingDao {
    <T> T getSetting(String channelName, IChannelSetting<T> channelSetting) throws ChannelSettingDAOException;
    <T> void setSetting(String channelName, IChannelSetting<T> channelSetting, T value) throws ChannelSettingDAOException;
}
