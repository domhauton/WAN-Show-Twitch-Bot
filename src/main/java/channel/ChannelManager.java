package channel;

import channel.data.TwitchMessage;
import channel.data.TwitchUser;
import channel.message.ImmutableTwitchMessageList;
import channel.message.MessageManager;
import channel.permissions.PermissionsManager;
import channel.permissions.UserPermission;

/**
 * Created by Dominic Hauton on 12/03/2016.
 *
 * Stores information about the user channel.
 */
public class ChannelManager {
    private PermissionsManager permissionsManager;
    private MessageManager     messageManager;

    public ChannelManager() {
        permissionsManager =     new PermissionsManager();
        messageManager =         new MessageManager();
    }

    /**
     * Checks if the given user has permission for the requested action per
     * @return true if user has permission for the action
     */
    public boolean checkPermission(TwitchUser user, UserPermission requiredPermission) {
        return permissionsManager.getUser(user).hasRequiredPermissions(requiredPermission);
    }

    public UserPermission getPermission(TwitchUser twitchUser) {
        return permissionsManager.getUser(twitchUser);
    }

    public UserPermission setPermission(TwitchUser twitchUser, UserPermission newPermission) {
        return permissionsManager.addUser(twitchUser, newPermission);
    }

    public ImmutableTwitchMessageList getMessageSnapshot() {
        return messageManager.getChannelSnapshot();
    }

    public ImmutableTwitchMessageList getMessageSnapshot(TwitchUser username) {
        return messageManager.getUserSnapshot(username);
    }

    public boolean addChannelMessage(TwitchMessage message) {
        return messageManager.addMessage(message);
    }
}
