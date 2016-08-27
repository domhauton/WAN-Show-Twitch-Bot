package bot.channel.permissions;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.stream.Stream;

import bot.channel.TwitchUser;

/**
 * Created by Dominic Hauton on 18/07/2016.
 * <p>
 * Check that permissions can be added and removed correctly.
 */
public class PermissionsManagerTest {

  private static TwitchUser twitchUser1 = new TwitchUser("fooUser1");
  private static TwitchUser twitchUser1_1 = new TwitchUser(twitchUser1.getUsername());
  private static TwitchUser twitchUser2 = new TwitchUser("fooUser2");
  private static TwitchUser twitchUser3 = new TwitchUser("fooUser3");
  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  private PermissionsManager permissionsManager;

  @Before
  public void setUp() throws Exception {
    permissionsManager = new PermissionsManager();
  }

  @Test
  public void retrievingEmptyUserTest() throws Exception {
    expectedException.expect(PermissionException.class);
    permissionsManager.getUser(twitchUser1);
  }

  @Test
  public void checkRetrievalByValueTest() throws Exception {
    permissionsManager.changeUserPermission(twitchUser1, UserPermission.ChannelOwner);
    UserPermission actualUserPermissions = permissionsManager.getUser(twitchUser1_1);
    UserPermission expectedUserPermissions = permissionsManager.getUser(twitchUser1);
    Assert.assertEquals("Should return the same permission even from different TwitchUser object", expectedUserPermissions, actualUserPermissions);
  }

  @Test
  public void addAndRetrieveMultipleUsersTest() throws Exception {
    permissionsManager.changeUserPermission(twitchUser1, UserPermission.ChannelOwner);
    permissionsManager.changeUserPermission(twitchUser2, UserPermission.ChannelUser);
    permissionsManager.changeUserPermission(twitchUser3, UserPermission.ChannelModerator);
    Assert.assertEquals("User 1 should be channel owner", UserPermission.ChannelOwner, permissionsManager.getUser(twitchUser1));
    Assert.assertEquals("User 2 should be channel user", UserPermission.ChannelUser, permissionsManager.getUser(twitchUser2));
    Assert.assertEquals("User 3 should be channel moderator", UserPermission.ChannelModerator, permissionsManager.getUser(twitchUser3));
  }

  @Test
  public void eachPermissionStoredCorrectlyTest() throws Exception {
    Stream.of(UserPermission.values())
        .forEach(userPermission -> setAndTestUserPermission(twitchUser1, userPermission));
  }

  private void setAndTestUserPermission(TwitchUser twitchUser, UserPermission userPermission) {
    permissionsManager.changeUserPermission(twitchUser, userPermission);
    try {
      Assert.assertEquals("Permission for " + twitchUser.toString() + " should be set to "
          + userPermission.toString(), userPermission, permissionsManager.getUser(twitchUser));
    } catch (PermissionException e) {
      throw new RuntimeException(e); // Cast to unchecked. Stream workaround -_-
    }

  }
}