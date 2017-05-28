package com.domhauton.wanbot.bot.channel.permissions;

import com.domhauton.wanbot.bot.channel.TwitchUser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

/**
 * Created by Dominic Hauton on 18/07/2016.
 * <p>
 * Check that permissions can be added and removed correctly.
 */
class PermissionsManagerTest {

  private static TwitchUser twitchUser1 = new TwitchUser("fooUser1");
  private static TwitchUser twitchUser1_1 = new TwitchUser(twitchUser1.getUsername());
  private static TwitchUser twitchUser2 = new TwitchUser("fooUser2");
  private static TwitchUser twitchUser3 = new TwitchUser("fooUser3");

  private PermissionsManager permissionsManager;

  @BeforeEach
  void setUp() throws Exception {
    permissionsManager = new PermissionsManager();
  }

  @Test
  void retrievingEmptyUserTest() throws Exception {
    Assertions.assertThrows(PermissionException.class, () -> permissionsManager.getUser(twitchUser1));
  }

  @Test
  void checkRetrievalByValueTest() throws Exception {
    permissionsManager.changeUserPermission(twitchUser1, UserPermission.ChannelOwner);
    UserPermission actualUserPermissions = permissionsManager.getUser(twitchUser1_1);
    UserPermission expectedUserPermissions = permissionsManager.getUser(twitchUser1);
    Assertions.assertEquals(expectedUserPermissions, actualUserPermissions, "Should return the same permission even from different TwitchUser object");
  }

  @Test
  void addAndRetrieveMultipleUsersTest() throws Exception {
    permissionsManager.changeUserPermission(twitchUser1, UserPermission.ChannelOwner);
    permissionsManager.changeUserPermission(twitchUser2, UserPermission.ChannelUser);
    permissionsManager.changeUserPermission(twitchUser3, UserPermission.ChannelModerator);
    Assertions.assertEquals(UserPermission.ChannelOwner, permissionsManager.getUser(twitchUser1), "User 1 should be channel owner");
    Assertions.assertEquals(UserPermission.ChannelUser, permissionsManager.getUser(twitchUser2), "User 2 should be channel user");
    Assertions.assertEquals(UserPermission.ChannelModerator, permissionsManager.getUser(twitchUser3), "User 3 should be channel moderator");
  }

  @Test
  void eachPermissionStoredCorrectlyTest() throws Exception {
    Stream.of(UserPermission.values())
        .forEach(userPermission -> setAndTestUserPermission(twitchUser1, userPermission));
  }

  private void setAndTestUserPermission(TwitchUser twitchUser, UserPermission userPermission) {
    permissionsManager.changeUserPermission(twitchUser, userPermission);
    try {
      Assertions.assertEquals(userPermission, permissionsManager.getUser(twitchUser),
          "Permission for " + twitchUser.toString() + " should be set to " + userPermission.toString());
    } catch (PermissionException e) {
      throw new RuntimeException(e); // Cast to unchecked. Stream workaround -_-
    }

  }
}