package twitch.channel;

import org.joda.time.Period;
import twitch.channel.blacklist.BlacklistManager;
import twitch.channel.data.TwitchMessage;
import twitch.channel.data.TwitchUser;
import twitch.channel.message.ImmutableTwitchMessageList;
import twitch.channel.message.MessageManager;
import twitch.channel.permissions.PermissionsManager;
import twitch.channel.permissions.UserPermission;
import twitch.channel.timeouts.TimeoutManager;

/**
 * Created by Dominic Hauton on 12/03/2016.
 *
 * Stores information about the user channel.
 */
public class ChannelManager {
    private PermissionsManager m_permissionsManager;
    private MessageManager m_messageManager;
    private TimeoutManager m_timeoutManager;
    private BlacklistManager m_blacklistManager;

    public ChannelManager() {
        m_permissionsManager = new PermissionsManager();
        m_messageManager = new MessageManager();
        m_timeoutManager = new TimeoutManager();
        m_blacklistManager = new BlacklistManager();
    }

    /**
     * Checks if the given user has permission for the requested action per
     * @return true if user has permission for the action
     */
    public boolean checkPermission(TwitchUser user, UserPermission requiredPermission) {
        return m_permissionsManager.getUser(user).hasRequiredPermissions(requiredPermission);
    }

    public UserPermission getPermission(TwitchUser twitchUser) {
        return m_permissionsManager.getUser(twitchUser);
    }

    public UserPermission setPermission(TwitchUser twitchUser, UserPermission newPermission) {
        return m_permissionsManager.addUser(twitchUser, newPermission);
    }

    public ImmutableTwitchMessageList getMessageSnapshot() {
        return m_messageManager.getChannelSnapshot();
    }

    public ImmutableTwitchMessageList getMessageSnapshot(TwitchUser username) {
        return m_messageManager.getUserSnapshot(username);
    }

    public Period getUserTimeout(TwitchUser twitchUser) {
        return m_timeoutManager.getUserTimeout(twitchUser);
    }

    public void addUserTimeout(TwitchUser twitchUser, Period timeoutPeriod){
        m_timeoutManager.addUserTimeout(twitchUser, timeoutPeriod);
    }

    public boolean addChannelMessage(TwitchMessage message) {
        return m_messageManager.addMessage(message);
    }

    public void blacklistMessage(String message){
        m_blacklistManager.blacklistMessage(message);
    }

    public void blacklistWord(String word){
        m_blacklistManager.addBlackListWord(word);
    }

    public void getBlacklist(){
        m_blacklistManager.getBlacklist();
    }
}
