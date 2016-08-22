package twitch.channel;

import org.joda.time.Duration;
import org.joda.time.Period;
import twitch.channel.blacklist.BlacklistEntry;
import twitch.channel.blacklist.BlacklistManager;
import twitch.channel.blacklist.BlacklistType;
import twitch.channel.message.ImmutableTwitchMessageList;
import twitch.channel.message.MessageManager;
import twitch.channel.message.TwitchMessage;
import twitch.channel.permissions.PermissionException;
import twitch.channel.permissions.PermissionsManager;
import twitch.channel.permissions.UserPermission;
import twitch.channel.settings.ChannelSettingDAOException;
import twitch.channel.settings.ChannelSettingDao;
import twitch.channel.settings.ChannelSettingDAOHashMapImpl;
import twitch.channel.settings.enums.ChannelSettingDouble;
import twitch.channel.settings.enums.ChannelSettingInteger;
import twitch.channel.settings.enums.ChannelSettingString;
import twitch.channel.timeouts.TimeoutManager;
import twitch.channel.timeouts.TimeoutReason;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
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
    private final ChannelSettingDao channelSettingDao;

    public ChannelManager(String channelName) {
        this(new ChannelSettingDAOHashMapImpl(), channelName);
    }

    ChannelManager(ChannelSettingDao channelSettingDao, String channelName) {
        this.channelName = channelName;
        m_permissionsManager = new PermissionsManager();
        m_messageManager = new MessageManager();
        m_timeoutManager = new TimeoutManager();
        m_blacklistManager = new BlacklistManager();
        this.channelSettingDao = channelSettingDao;
    }

    /**
     * Checks if the given user has permission for the requested action per
     * @return true if user has permission for the action
     */
    public boolean checkPermission(TwitchUser user, UserPermission requiredPermission) {
        return getPermission(user).authorizedForActionOfPermissionLevel(requiredPermission);
    }

    public UserPermission getPermission(TwitchUser twitchUser) {
        try {
            return m_permissionsManager.getUser(twitchUser);
        } catch (PermissionException e) {
            String defaultPermissionString = channelSettingDao.getSettingOrDefault(channelName, ChannelSettingString
                    .DEFAULT_PERMISSION);
            try {
                return UserPermission.valueOf(defaultPermissionString);
            } catch (IllegalArgumentException e2) {
                return UserPermission.ChannelUser;
            }
        }
    }

    public void setPermission(TwitchUser twitchUser, UserPermission newPermission) {
        m_permissionsManager.changeUserPermission(twitchUser, newPermission);
    }

    public ImmutableTwitchMessageList getMessageSnapshot() {
        return m_messageManager.getChannelSnapshot();
    }

    public ImmutableTwitchMessageList getMessageSnapshot(TwitchUser username) {
        return m_messageManager.getUserSnapshot(username);
    }

    public Duration getUserTimeout(TwitchUser twitchUser) {
        return m_timeoutManager.getUserTimeout(twitchUser.getUsername());
    }

    public Duration addUserTimeout(String twitchUser, TimeoutReason timeoutReason){
        return m_timeoutManager.addUserTimeout(twitchUser, timeoutReason);
    }

    public boolean addChannelMessage(TwitchMessage message) {
        return m_messageManager.addMessage(message);
    }

    public Collection<TwitchMessage> blacklistItem(String input, BlacklistType blacklistType) throws
            ChannelOperationException {
        Integer messageLookBehind = channelSettingDao.getSetting(channelName, ChannelSettingInteger.CHANNEL_RETROSPECTIVE_LOOKBACK);
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
            BlacklistEntry blacklistEntry = m_blacklistManager.addToBlacklist(input, blacklistType);
            return trimmedMessageList
                    .stream()
                    .filter(message -> blacklistEntry.matches(message.getMessage()))
                    .collect(Collectors.toList());
        } else {
            throw new ChannelOperationException("Blacklist look-behind must be 0-" + messageList.size()
                                                + ". Value:" + messageLookBehind);
        }

    }

    public String getChannelName() {
        return channelName;
    }
}
