package twitch.channel.settings;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Dominic Hauton on 24/07/2016.
 *
 * Abstract way for the DAO to access
 */
public abstract class ChannelSettingSupplierTest {

    protected ChannelSettingDAO m_channelSettingDAO;

    protected static final String s_channelName1 = "channel1";
    protected static final String s_channelName2 = "channel2";

    @Test
    public void simpleInsertRetrieveTest() throws Exception {
        m_channelSettingDAO.setDoubleSetting(s_channelName1, ChannelSettingDouble.CHANNEL_LOOKBACK, 50d);
        Assert.assertEquals("Should return 50d. Has just been set.", 50d, m_channelSettingDAO.getDoubleSetting(s_channelName1, ChannelSettingDouble.CHANNEL_LOOKBACK), 0);
    }
}