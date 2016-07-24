package twitch.channel.settings;

/**
 * Created by Dominic Hauton on 24/07/2016.
 *
 * Double
 */
public enum ChannelSettingDouble {
    CHANNEL_LOOKBACK(50d);

    private Double defaultValue;

    ChannelSettingDouble(Double defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Double getDefaultValue() {
        return defaultValue;
    }

}
