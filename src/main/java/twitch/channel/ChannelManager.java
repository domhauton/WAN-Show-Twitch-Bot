package twitch.channel;

import twitch.channel.data.TwitchMessage;
import twitch.channel.data.TwitchUser;
import twitch.channel.message.ImmutableTwitchMessageList;
import twitch.channel.message.MessageManager;
import twitch.channel.permissions.PermissionsManager;
import twitch.channel.permissions.UserPermission;

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
