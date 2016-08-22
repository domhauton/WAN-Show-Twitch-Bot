package twitch.channel.settings;

import org.junit.Before;

import static org.junit.Assert.*;

/**
 * Created by Dominic Hauton on 26/07/2016.
 *
 * An implementation of the DOA interface test for the HashMap Implementation
 */
public class IChannelSettingDAOHashMapImplTest extends ChannelSettingSupplierTest{
    @Override
    @Before
    public void setUp() throws Exception {
        m_channelSettingDao = new ChannelSettingDAOHashMapImpl();
        super.setUp();
    }
}