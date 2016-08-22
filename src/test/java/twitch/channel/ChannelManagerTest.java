package twitch.channel;

import org.junit.Before;
import twitch.channel.settings.ChannelSettingDAOHashMapImpl;

/**
 * Created by Dominic Hauton on 22/08/2016.
 * <p>
 * Main test for all channel manager methods.
 */
public class ChannelManagerTest {

    private ChannelManager m_channelManager;

    @Before
    public void setUp() throws Exception {
        m_channelManager = new ChannelManager(new ChannelSettingDAOHashMapImpl(), "fooChannel");
    }

    //TODO Implement Tests
}