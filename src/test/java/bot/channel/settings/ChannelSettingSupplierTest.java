package bot.channel.settings;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import bot.channel.settings.enums.ChannelSettingDouble;
import bot.channel.settings.enums.ChannelSettingInteger;
import bot.channel.settings.enums.ChannelSettingString;

/**
 * Created by Dominic Hauton on 24/07/2016.
 *
 * Abstract way for the DAO to access
 */
public abstract class ChannelSettingSupplierTest {
  protected static final String channelName1 = "channel1";
  @Rule
  public final ExpectedException expectedException = ExpectedException.none();
  protected ChannelSettingDao channelSettingDao;

  @Before
  public void setUp() throws Exception {
    Assert.assertNotNull("Please instantiate a channelSettingDAO in your test class!", channelSettingDao);
  }

  @Test
  public void simpleInsertRetrieveIntegerTest() throws Exception {
    channelSettingDao.setSetting(channelName1, ChannelSettingInteger.CHANNEL_RETROSPECTIVE_LOOKBACK, 50);
    Integer actualValue = channelSettingDao.getSetting(channelName1, ChannelSettingInteger.CHANNEL_RETROSPECTIVE_LOOKBACK);
    Assert.assertEquals("Should return 50. Has just been set.", Integer.valueOf(50), actualValue);
  }

  @Test
  public void repeatedInsertRetrieveIntegerTest() throws Exception {
    simpleInsertRetrieveIntegerTest();

    channelSettingDao.setSetting(channelName1, ChannelSettingInteger.CHANNEL_RETROSPECTIVE_LOOKBACK, 45);
    Integer actualValue2 = channelSettingDao.getSetting(channelName1, ChannelSettingInteger
        .CHANNEL_RETROSPECTIVE_LOOKBACK);
    Assert.assertEquals("Should return 45. Has just been set.", Integer.valueOf(45), actualValue2);
  }

  @Test
  public void simpleInsertRetrieveDoubleTest() throws Exception {
    channelSettingDao.setSetting(channelName1, ChannelSettingDouble.MAX_MESSAGE_RATE, 50d);
    Double actualValue1 = channelSettingDao.getSetting(channelName1, ChannelSettingDouble.MAX_MESSAGE_RATE);
    Assert.assertEquals("Should return 50. Has just been set.", 50d, actualValue1, 0);
  }

  @Test
  public void repeatedInsertRetrieveDoubleTest() throws Exception {
    simpleInsertRetrieveDoubleTest();
    channelSettingDao.setSetting(channelName1, ChannelSettingDouble.MAX_MESSAGE_RATE, 45d);
    Double actualValue2 = channelSettingDao.getSetting(channelName1, ChannelSettingDouble.MAX_MESSAGE_RATE);
    Assert.assertEquals("Should return 45. Has just been set.", 45d, actualValue2, 0);
  }

  @Test
  public void ensureDoubleDefaultMatchGetOrDefaultTest() throws Exception {
    Double expectedDefault = ChannelSettingDouble.MAX_MESSAGE_RATE.getDefault();
    Double actualDefault = channelSettingDao.getSettingOrDefault(channelName1, ChannelSettingDouble.MAX_MESSAGE_RATE);
    Assert.assertEquals(expectedDefault, actualDefault);
  }

  @Test
  public void simpleInsertRetrieveStringTest() throws Exception {
    String newValue = "ChannelUserFoo";
    channelSettingDao.setSetting(channelName1, ChannelSettingString.DEFAULT_PERMISSION, newValue);
    String actualValue = channelSettingDao.getSetting(channelName1, ChannelSettingString.DEFAULT_PERMISSION);
    Assert.assertEquals("Should return 50. Has just been set.", newValue, actualValue);
  }

  @Test
  public void retrieveBeforeInsertTest() throws Exception {
    expectedException.expect(ChannelSettingDAOException.class);
    channelSettingDao.getSetting(channelName1, ChannelSettingDouble.MAX_MESSAGE_RATE);
  }
}