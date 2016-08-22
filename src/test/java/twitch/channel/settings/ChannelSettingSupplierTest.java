package twitch.channel.settings;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import twitch.channel.settings.enums.ChannelSettingDouble;
import twitch.channel.settings.enums.ChannelSettingInteger;

/**
 * Created by Dominic Hauton on 24/07/2016.
 *
 * Abstract way for the DAO to access
 */
public abstract class ChannelSettingSupplierTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    protected ChannelSettingDao m_channelSettingDao;

    protected static final String s_channelName1 = "channel1";
    protected static final String s_channelName2 = "channel2";

    @Before
    public void setUp() throws Exception {
        Assert.assertNotNull("Please instantiate a channelSettingDAO in your test class!", m_channelSettingDao);
    }

    @Test
    public void simpleInsertRetrieveIntegerTest() throws Exception {
        m_channelSettingDao.setSetting(s_channelName1, ChannelSettingInteger.CHANNEL_RETROSPECTIVE_LOOKBACK, 50);
        Integer actualValue = m_channelSettingDao.getSetting(s_channelName1, ChannelSettingInteger.CHANNEL_RETROSPECTIVE_LOOKBACK);
        Assert.assertEquals("Should return 50. Has just been set.", Integer.valueOf(50), actualValue);
    }

    @Test
    public void repeatedInsertRetrieveIntegerTest() throws Exception {
        simpleInsertRetrieveIntegerTest();

        m_channelSettingDao.setSetting(s_channelName1, ChannelSettingInteger.CHANNEL_RETROSPECTIVE_LOOKBACK, 45);
        Integer actualValue2 = m_channelSettingDao.getSetting(s_channelName1, ChannelSettingInteger
                .CHANNEL_RETROSPECTIVE_LOOKBACK);
        Assert.assertEquals("Should return 45. Has just been set.", Integer.valueOf(45), actualValue2);
    }

    @Test
    public void simpleInsertRetrieveDoubleTest() throws Exception {
        m_channelSettingDao.setSetting(s_channelName1, ChannelSettingDouble.MAX_MESSAGE_RATE, 50d);
        Double actualValue1 = m_channelSettingDao.getSetting(s_channelName1, ChannelSettingDouble.MAX_MESSAGE_RATE);
        Assert.assertEquals("Should return 50. Has just been set.", 50d, actualValue1, 0);
    }

    @Test
    public void repeatedInsertRetrieveDoubleTest() throws Exception {
        simpleInsertRetrieveDoubleTest();

        m_channelSettingDao.setSetting(s_channelName1, ChannelSettingDouble.MAX_MESSAGE_RATE, 45d);
        Double actualValue2 = m_channelSettingDao.getSetting(s_channelName1, ChannelSettingDouble.MAX_MESSAGE_RATE);
        Assert.assertEquals("Should return 45. Has just been set.", 45d, actualValue2, 0);
    }

    @Test
    public void retrieveBeforeInsertTest() throws Exception {
        expectedException.expect(ChannelSettingDAOException.class);
        m_channelSettingDao.getSetting(s_channelName1, ChannelSettingDouble.MAX_MESSAGE_RATE);
    }
}