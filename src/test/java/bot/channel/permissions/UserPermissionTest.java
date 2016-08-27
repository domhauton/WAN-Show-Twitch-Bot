package bot.channel.permissions;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Dominic Hauton on 18/07/2016.
 *
 * Testing authorisation works in expected direction
 */
public class UserPermissionTest {

    @Test
    public void authorizedForActionOfPermissionLevelLessThan() throws Exception {
        Assert.assertTrue("ChannelOwner should have access to BotAdmin", UserPermission.ChannelOwner
                .authorizedForActionOfPermissionLevel(UserPermission.BotAdmin));
    }

    @Test
    public void authorizedForActionOfPermissionLevelEqualTo() throws Exception {
        Assert.assertTrue("ChannelOwner should have access to ChannelAdmin Actions", UserPermission.ChannelOwner
                .authorizedForActionOfPermissionLevel(UserPermission.ChannelOwner));
    }

    @Test
    public void unauthorizedForActionOfPermissionLevelGreaterThan() throws Exception {
        Assert.assertFalse("ChannelOwner should have access to BotAdmin", UserPermission.BotAdmin
                .authorizedForActionOfPermissionLevel(UserPermission.ChannelOwner));
    }

}