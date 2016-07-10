package twitch.channel.settings;

/**
 * Created by Dominic Hauton on 26/06/2016.
 */
public class ChannelSettingDao {
    public final ChannelSetting<Integer> messageLookBehind;

    public ChannelSettingDao(ChannelSettingSupplier channelSettingSupplier, String channelName) {
        this.messageLookBehind = new ChannelSetting<>(50, "messageLookBehind", channelName, channelSettingSupplier);
    }
}
