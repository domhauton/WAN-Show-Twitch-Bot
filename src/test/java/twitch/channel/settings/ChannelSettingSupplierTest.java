package twitch.channel.settings;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import twitch.channel.settings.enums.ChannelSettingDouble;
import twitch.channel.settings.enums.ChannelSettingInteger;

/**
 * Created by Dominic Hauton on 24/07/2016.
 *
 * Abstract way for the DAO to access
 */
public abstract class ChannelSettingSupplierTest {

    protected ChannelSettingDao m_channelSettingDao;

    protected static final String s_channelName1 = "channel1";
    protected static final String s_channelName2 = "channel2";

    @Before
    public void setUp() throws Exception {
        Assert.assertNotNull("Please instantiate a channelSettingDAO in your test class!", m_channelSettingDao);
    }

    @Test
    public void simpleInsertRetrieveTest() throws Exception {
        m_channelSettingDao.setSetting(s_channelName1, ChannelSettingInteger.CHANNEL_RETROSPECTIVE_LOOKBACK, 50);
        Assert.assertEquals("Should return 50. Has just been set.", 50, m_channelSettingDao.getSetting(s_channelName1,
                ChannelSettingInteger.CHANNEL_RETROSPECTIVE_LOOKBACK), 0);
    }
}