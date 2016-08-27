package bot.channel.settings.enums;

/**
 * Created by Dominic Hauton on 10/08/2016.
 *
 * Stores a list of possible String channel settings.
 */
public enum ChannelSettingString implements IChannelSetting<String> {
  DEFAULT_PERMISSION("ChannelUser");

  private String defaultValue;

  ChannelSettingString(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  @Override
  public String getDefault() {
    return defaultValue;
  }

  @Override
  public Class getGenericInterfaceType() {
    return String.class;
  }
}
