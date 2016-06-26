package twitch.channel.settings;

/**
 * Created by Dominic Hauton on 26/06/2016.
 */
public class ChannelSettingDao {
    public final ChannelSetting<Integer> lookback;

    public ChannelSettingDao(ChannelSettingSupplier channelSettingSupplier) {
        this.lookback = new ChannelSetting<>(50, "lookback", channelSettingSupplier);
    }
}
