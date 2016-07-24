package twitch.channel.permissions;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Dominic Hauton on 18/07/2016.
 */
public class UserPermissionTest {

    @Test
    public void testDefaultPermission() throws Exception {
        Assert.assertFalse("Default permission should always be less than any mod/admin/owner rank.",
                UserPermission.getDefaultPermission().authorizedForActionOfPermissionLevel(UserPermission.ChannelOwner) ||
                UserPermission.getDefaultPermission().authorizedForActionOfPermissionLevel(UserPermission.ChannelModerator) ||
                UserPermission.getDefaultPermission().authorizedForActionOfPermissionLevel(UserPermission.BotAdmin) ||
                UserPermission.getDefaultPermission().authorizedForActionOfPermissionLevel(UserPermission.BotModerator));
    }

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