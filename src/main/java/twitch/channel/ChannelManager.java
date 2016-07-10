package twitch.channel;

import org.joda.time.Period;
import twitch.channel.blacklist.BlacklistManager;
import twitch.channel.blacklist.BlacklistType;
import twitch.channel.message.ImmutableTwitchMessageList;
import twitch.channel.message.MessageManager;
import twitch.channel.message.TwitchMessage;
import twitch.channel.permissions.PermissionsManager;
import twitch.channel.permissions.UserPermission;
import twitch.channel.settings.ChannelSettingDao;
import twitch.channel.settings.ChannelSettingSupplier;
import twitch.channel.settings.suppliers.ChannelSettingsHashMap;
import twitch.channel.timeouts.TimeoutManager;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Created by Dominic Hauton on 12/03/2016.
 *
 * Stores information about the user channel.
 */
public class ChannelManager {
    private final String channelName;
    private final PermissionsManager m_permissionsManager;
    private final MessageManager m_messageManager;
    private final TimeoutManager m_timeoutManager;
    private final BlacklistManager m_blacklistManager;
    private final ChannelSettingDao m_channelChannelSettingDao;

    public ChannelManager(String channelName) {
        this(new ChannelSettingsHashMap(), channelName);
    }

    ChannelManager(ChannelSettingSupplier channelSettingSupplier, String channelName) {
        this.channelName = channelName;
        m_permissionsManager = new PermissionsManager();
        m_messageManager = new MessageManager();
        m_timeoutManager = new TimeoutManager();
        m_blacklistManager = new BlacklistManager();
        m_channelChannelSettingDao = new ChannelSettingDao(channelSettingSupplier, channelName);
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

    public Collection<TwitchMessage> blacklistItem(String input, BlacklistType blacklistType) throws
            ChannelOperationException {
        Integer messageLookBehind = m_channelChannelSettingDao.messageLookBehind.getOrDefault();
        return blacklistItem(input, blacklistType, messageLookBehind);
    }

    /**
    * @return List of messages breaching new item.
    */
    public Collection<TwitchMessage> blacklistItem(
            String input,
            BlacklistType blacklistType,
            int messageLookBehind) throws ChannelOperationException {
        ImmutableTwitchMessageList messageList = getMessageSnapshot();
        if ( messageLookBehind == 0 ) {
            m_blacklistManager.addToBlacklist(input, blacklistType);
            return Collections.emptyList();
        } else if ( messageLookBehind > 0 && messageLookBehind <= messageList.size() ) {
            Collection<TwitchMessage> trimmedMessageList = messageList
                    .stream()
                    .limit(messageLookBehind)
                    .collect(Collectors.toList());
            return m_blacklistManager.addToBlacklist(input, blacklistType, trimmedMessageList);
        } else {
            throw new ChannelOperationException("Blacklist look-behind must be 0-" + messageList.size()
                                                + ". Value:" + messageLookBehind);
        }

    }

    public String getChannelName() {
        return channelName;
    }
}
