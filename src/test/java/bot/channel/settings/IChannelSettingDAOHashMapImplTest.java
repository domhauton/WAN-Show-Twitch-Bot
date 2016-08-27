package bot.channel.settings;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import bot.channel.settings.enums.ChannelSettingString;

/**
 * Created by Dominic Hauton on 26/07/2016.
 *
 * An implementation of the DOA interface test for the HashMap Implementation
 */
public class IChannelSettingDAOHashMapImplTest extends ChannelSettingSupplierTest {

  @Override
  @Before
  public void setUp() throws Exception {
    channelSettingDao = new ChannelSettingDAOHashMapImpl();
    super.setUp();
  }

  @Test
  public void castErrorWrongObjectTest() throws Exception {
    HashMap<String, Object> settingMap = new HashMap<>();
    String key = ChannelSettingDAOHashMapImpl.generateKey(channelName1, ChannelSettingString.DEFAULT_PERMISSION);
    settingMap.put(key, 10d);
    ChannelSettingDAOHashMapImpl channelSettingDAO = new ChannelSettingDAOHashMapImpl(settingMap);
    expectedException.expect(ChannelSettingDAOException.class);
    String setting = channelSettingDAO.getSetting(channelName1, ChannelSettingString.DEFAULT_PERMISSION);
    System.out.println(setting);
  }
}