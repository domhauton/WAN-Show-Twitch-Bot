package twitch.channel.settings;

/**
 * Created by Dominic Hauton on 05/06/2016.
 *
 * An interface to be implemented by all Persistence layers
 */
public interface ChannelSettingDao {
    Double getDoubleSetting(String channelName, ChannelSettingDouble channelSettingDouble) throws ChannelSettingDAOException;

    void setDoubleSetting(String channelName, ChannelSettingDouble channelSettingDouble, Double value) throws ChannelSettingDAOException;
}
