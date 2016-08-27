package bot.channel.settings.enums;

/**
 * Created by Dominic Hauton on 24/07/2016.
 *
 * Double
 */
public enum ChannelSettingDouble implements IChannelSetting<Double> {
    MAX_MESSAGE_RATE(50d);

    private Double defaultValue;

    ChannelSettingDouble(Double defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Double getDefault() {
        return defaultValue;
    }

    public Class<Double> getGenericInterfaceType() {
        return Double.class;
    }
}
