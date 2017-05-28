package bot.channel.settings;

import bot.channel.settings.enums.ChannelSettingString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

/**
 * Created by Dominic Hauton on 26/07/2016.
 * <p>
 * An implementation of the DOA interface test for the HashMap Implementation
 */
public class IChannelSettingDAOHashMapImplTest extends ChannelSettingSupplierTest {

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    channelSettingDao = new ChannelSettingDAOHashMapImpl();
    super.setUp();
  }

  @Test
  void castErrorWrongObjectTest() throws Exception {
    HashMap<String, Object> settingMap = new HashMap<>();
    String key = ChannelSettingDAOHashMapImpl.generateKey(channelName1, ChannelSettingString.DEFAULT_PERMISSION);
    settingMap.put(key, 10d);
    ChannelSettingDAOHashMapImpl channelSettingDAO = new ChannelSettingDAOHashMapImpl(settingMap);
    Assertions.assertThrows(ChannelSettingDAOException.class,
        () -> channelSettingDAO.getSetting(channelName1, ChannelSettingString.DEFAULT_PERMISSION));
  }
}