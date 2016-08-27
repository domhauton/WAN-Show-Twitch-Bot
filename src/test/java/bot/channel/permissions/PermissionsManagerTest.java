package bot.channel.permissions;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import bot.channel.TwitchUser;

import java.util.stream.Stream;

/**
 * Created by Dominic Hauton on 18/07/2016.
 * <p>
 * Check that permissions can be added and removed correctly.
 */
public class PermissionsManagerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static TwitchUser s_twitchUser1 = new TwitchUser("fooUser1");
    private static TwitchUser s_twitchUser1_1 = new TwitchUser(s_twitchUser1.getUsername());
    private static TwitchUser s_twitchUser2 = new TwitchUser("fooUser2");
    private static TwitchUser s_twitchUser3 = new TwitchUser("fooUser3");

    private PermissionsManager m_permissionsManager;

    @Before
    public void setUp() throws Exception {
        m_permissionsManager = new PermissionsManager();
    }

    @Test
    public void retrievingEmptyUserTest() throws Exception {
        expectedException.expect(PermissionException.class);
        m_permissionsManager.getUser(s_twitchUser1);
    }

    @Test
    public void checkRetrievalByValueTest() throws Exception {
        m_permissionsManager.changeUserPermission(s_twitchUser1, UserPermission.ChannelOwner);
        UserPermission actualUserPermissions = m_permissionsManager.getUser(s_twitchUser1_1);
        UserPermission expectedUserPermissions = m_permissionsManager.getUser(s_twitchUser1);
        Assert.assertEquals("Should return the same permission even from different TwitchUser object", expectedUserPermissions, actualUserPermissions);
    }

    @Test
    public void addAndRetrieveMultipleUsersTest() throws Exception {
        m_permissionsManager.changeUserPermission(s_twitchUser1, UserPermission.ChannelOwner);
        m_permissionsManager.changeUserPermission(s_twitchUser2, UserPermission.ChannelUser);
        m_permissionsManager.changeUserPermission(s_twitchUser3, UserPermission.ChannelModerator);
        Assert.assertEquals("User 1 should be channel owner", UserPermission.ChannelOwner, m_permissionsManager.getUser(s_twitchUser1));
        Assert.assertEquals("User 2 should be channel user", UserPermission.ChannelUser, m_permissionsManager.getUser(s_twitchUser2));
        Assert.assertEquals("User 3 should be channel moderator", UserPermission.ChannelModerator, m_permissionsManager.getUser(s_twitchUser3));
    }

    @Test
    public void eachPermissionStoredCorrectlyTest() throws Exception {
        Stream.of(UserPermission.values())
                .forEach(userPermission -> setAndTestUserPermission(s_twitchUser1, userPermission));
    }

    private void setAndTestUserPermission(TwitchUser twitchUser, UserPermission userPermission) {
        m_permissionsManager.changeUserPermission(twitchUser, userPermission);
        try{
            Assert.assertEquals("Permission for " + twitchUser.toString() + " should be set to "
                                + userPermission.toString(), userPermission, m_permissionsManager.getUser(twitchUser));
        } catch (PermissionException e) {
            throw new RuntimeException(e); // Cast to unchecked. Stream workaround -_-
        }

    }
}