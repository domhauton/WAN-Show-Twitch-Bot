package bot.channel.permissions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by Dominic Hauton on 18/07/2016.
 * <p>
 * Testing authorisation works in expected direction
 */
class UserPermissionTest {

  @Test
  void authorizedForActionOfPermissionLevelLessThan() throws Exception {
    Assertions.assertTrue(UserPermission.ChannelOwner.authorizedForActionOfPermissionLevel(UserPermission.BotAdmin),
        "ChannelOwner should have access to BotAdmin");
  }

  @Test
  void authorizedForActionOfPermissionLevelEqualTo() throws Exception {
    Assertions.assertTrue(UserPermission.ChannelOwner.authorizedForActionOfPermissionLevel(UserPermission.ChannelOwner),
        "ChannelOwner should have access to ChannelAdmin Actions");
  }

  @Test
  void unauthorizedForActionOfPermissionLevelGreaterThan() throws Exception {
    Assertions.assertFalse(UserPermission.BotAdmin.authorizedForActionOfPermissionLevel(UserPermission.ChannelOwner),
        "ChannelOwner should have access to BotAdmin");
  }

}