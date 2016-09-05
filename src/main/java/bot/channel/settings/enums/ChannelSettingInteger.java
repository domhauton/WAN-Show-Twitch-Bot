package bot.channel.settings.enums;

/**
 * Created by Dominic Hauton on 24/07/2016.
 *
 * Double
 */
public enum ChannelSettingInteger implements IChannelSetting<Integer> {
  CHANNEL_RETROSPECTIVE_LOOKBACK(50),
  LINK_REPEAT_COUNT(5);

  private Integer defaultValue;

  ChannelSettingInteger(Integer defaultValue) {
    this.defaultValue = defaultValue;
  }

  public Integer getDefault() {
    return defaultValue;
  }

  public Class<Integer> getGenericInterfaceType() {
    return Integer.class;
  }
}
