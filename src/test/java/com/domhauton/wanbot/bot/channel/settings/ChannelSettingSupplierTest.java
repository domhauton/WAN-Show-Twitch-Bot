package com.domhauton.wanbot.bot.channel.settings;

import com.domhauton.wanbot.bot.channel.settings.enums.ChannelSettingDouble;
import com.domhauton.wanbot.bot.channel.settings.enums.ChannelSettingInteger;
import com.domhauton.wanbot.bot.channel.settings.enums.ChannelSettingString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by Dominic Hauton on 24/07/2016.
 * <p>
 * Abstract way for the DAO to access
 */
public abstract class ChannelSettingSupplierTest {
  static final String channelName1 = "channel1";
  ChannelSettingDao channelSettingDao;

  @BeforeEach
  public void setUp() throws Exception {
    Assertions.assertNotNull(channelSettingDao, "Please instantiate a channelSettingDAO in your test class!");
  }

  @Test
  void simpleInsertRetrieveIntegerTest() throws Exception {
    channelSettingDao.setSetting(channelName1, ChannelSettingInteger.CHANNEL_RETROSPECTIVE_LOOKBACK, 50);
    Integer actualValue = channelSettingDao.getSetting(channelName1, ChannelSettingInteger.CHANNEL_RETROSPECTIVE_LOOKBACK);
    Assertions.assertEquals(Integer.valueOf(50), actualValue, "Should return 50. Has just been set.");
  }

  @Test
  void repeatedInsertRetrieveIntegerTest() throws Exception {
    simpleInsertRetrieveIntegerTest();

    channelSettingDao.setSetting(channelName1, ChannelSettingInteger.CHANNEL_RETROSPECTIVE_LOOKBACK, 45);
    Integer actualValue2 = channelSettingDao.getSetting(channelName1, ChannelSettingInteger
        .CHANNEL_RETROSPECTIVE_LOOKBACK);
    Assertions.assertEquals(Integer.valueOf(45), actualValue2, "Should return 45. Has just been set.");
  }

  @Test
  void simpleInsertRetrieveDoubleTest() throws Exception {
    channelSettingDao.setSetting(channelName1, ChannelSettingDouble.MAX_MESSAGE_RATE, 50d);
    Double actualValue1 = channelSettingDao.getSetting(channelName1, ChannelSettingDouble.MAX_MESSAGE_RATE);
    Assertions.assertEquals(50d, actualValue1, 0.01, "Should return 50. Has just been set.");
  }

  @Test
  void repeatedInsertRetrieveDoubleTest() throws Exception {
    simpleInsertRetrieveDoubleTest();
    channelSettingDao.setSetting(channelName1, ChannelSettingDouble.MAX_MESSAGE_RATE, 45d);
    Double actualValue2 = channelSettingDao.getSetting(channelName1, ChannelSettingDouble.MAX_MESSAGE_RATE);
    Assertions.assertEquals(45d, actualValue2, 0.01, "Should return 45. Has just been set.");
  }

  @Test
  void ensureDoubleDefaultMatchGetOrDefaultTest() throws Exception {
    Double expectedDefault = ChannelSettingDouble.MAX_MESSAGE_RATE.getDefault();
    Double actualDefault = channelSettingDao.getSettingOrDefault(channelName1, ChannelSettingDouble.MAX_MESSAGE_RATE);
    Assertions.assertEquals(expectedDefault, actualDefault);
  }

  @Test
  void simpleInsertRetrieveStringTest() throws Exception {
    String newValue = "ChannelUserFoo";
    channelSettingDao.setSetting(channelName1, ChannelSettingString.DEFAULT_PERMISSION, newValue);
    String actualValue = channelSettingDao.getSetting(channelName1, ChannelSettingString.DEFAULT_PERMISSION);
    Assertions.assertEquals(newValue, actualValue, "Should return 50. Has just been set.");
  }

  @Test
  void retrieveBeforeInsertTest() throws Exception {
    Assertions.assertThrows(ChannelSettingDAOException.class,
        () -> channelSettingDao.getSetting(channelName1, ChannelSettingDouble.MAX_MESSAGE_RATE));
  }
}