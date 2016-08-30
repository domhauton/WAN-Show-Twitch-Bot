package bot.channel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Duration;
import bot.channel.blacklist.BlacklistEntry;
import bot.channel.blacklist.BlacklistManager;
import bot.channel.blacklist.BlacklistOperationException;
import bot.channel.blacklist.BlacklistType;
import bot.channel.message.ImmutableTwitchMessageList;
import bot.channel.message.MessageManager;
import bot.channel.message.TwitchMessage;
import bot.channel.permissions.PermissionException;
import bot.channel.permissions.PermissionsManager;
import bot.channel.permissions.UserPermission;
import bot.channel.settings.ChannelSettingDao;
import bot.channel.settings.ChannelSettingDAOHashMapImpl;
import bot.channel.settings.enums.ChannelSettingInteger;
import bot.channel.settings.enums.ChannelSettingString;
import bot.channel.timeouts.TimeoutManager;
import bot.channel.timeouts.TimeoutReason;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Created by Dominic Hauton on 12/03/2016.
 *
 * Stores information about the user channel.
 */
public class ChannelManager {
    private final String m_channelName;
    private final PermissionsManager m_permissionsManager;
    private final MessageManager m_messageManager;
    private final TimeoutManager m_timeoutManager;
    private final BlacklistManager m_blacklistManager;
    private final ChannelSettingDao m_channelSettingDao;

    private static final Logger s_log = LogManager.getLogger();

    public ChannelManager(String channelName) {
        this(channelName,
                new PermissionsManager(),
                new MessageManager(),
                new TimeoutManager(),
                new BlacklistManager(),
                new ChannelSettingDAOHashMapImpl());
    }

    ChannelManager(
            String channelName,
            PermissionsManager permissionsManager,
            MessageManager messageManager,
            TimeoutManager timeoutManager,
            BlacklistManager blacklistManager,
            ChannelSettingDao channelSettingDao) {
        m_channelName = channelName;
        m_permissionsManager = permissionsManager;
        m_messageManager = messageManager;
        m_timeoutManager = timeoutManager;
        m_blacklistManager = blacklistManager;
        m_channelSettingDao = channelSettingDao;
    }

    /**
     * Checks if the given user has permission for the requested action per
     * @return true if user has permission for the action
     */
    boolean checkPermission(TwitchUser user, UserPermission requiredPermission) throws ChannelOperationException {
        return getPermission(user).authorizedForActionOfPermissionLevel(requiredPermission);
    }

    public UserPermission getPermission(TwitchUser twitchUser) throws ChannelOperationException {
        try {
            return m_permissionsManager.getUser(twitchUser);
        } catch (PermissionException e) {
            String defaultPermissionString = m_channelSettingDao.getSettingOrDefault(m_channelName, ChannelSettingString
                    .DEFAULT_PERMISSION);
            try{
                return UserPermission.valueOf(defaultPermissionString);
            } catch (IllegalArgumentException e2) {
                throw new ChannelOperationException("Failed to cast default permission to valid permission");
            }
        }
    }

    public void setPermission(TwitchUser twitchUser, UserPermission newPermission) {
        s_log.info("Setting permission {} for user {}", newPermission::toString, twitchUser::toString);
        m_permissionsManager.changeUserPermission(twitchUser, newPermission);
    }

    /**
     * Adds a message to the channel message manager.
     * @return true if message passed blacklists.
     * @throws ChannelOperationException insertion failed. Reason unknown.
     */
    public boolean addChannelMessage(TwitchMessage message) throws ChannelOperationException {
        if (m_messageManager.addMessage(message)) {
            // n.b. Inverted boolean!
            return !m_blacklistManager.isMessageBlacklisted(message.getMessage());
        } else {
            throw new ChannelOperationException("Failed to insert message into channel. Reason Unknown.");
        }
    }

    public ImmutableTwitchMessageList getMessageSnapshot() {
        return m_messageManager.getChannelSnapshot();
    }

    public ImmutableTwitchMessageList getMessageSnapshot(TwitchUser username) {
        return m_messageManager.getUserSnapshot(username);
    }

    Duration getUserTimeout(TwitchUser twitchUser) {
        return m_timeoutManager.getUserTimeout(twitchUser.getUsername());
    }

    public Duration addUserTimeout(String twitchUser, TimeoutReason timeoutReason){
        s_log.info("Adding a timeout {} for user {}", timeoutReason::toString, twitchUser::toString);
        return m_timeoutManager.addUserTimeout(twitchUser, timeoutReason);
    }

    public Collection<TwitchMessage> blacklistItem(String input, BlacklistType blacklistType) throws
            ChannelOperationException {
        Integer messageLookBehind = m_channelSettingDao.getSettingOrDefault(m_channelName, ChannelSettingInteger
                .CHANNEL_RETROSPECTIVE_LOOKBACK);
        return blacklistItem(input, blacklistType, messageLookBehind);
    }

    /**
    * @return List of messages breaching new item.
    */
    public Collection<TwitchMessage> blacklistItem(
            String input,
            BlacklistType blacklistType,
            int messageLookBehind) throws ChannelOperationException {
        s_log.info("Adding item {} to channel {} blacklist as {} with {} look behind", input, m_channelName,
                blacklistType, messageLookBehind);
        ImmutableTwitchMessageList messageList = getMessageSnapshot();
        if ( messageLookBehind <= 0 ) {
            m_blacklistManager.addToBlacklist(input, blacklistType);
            return Collections.emptyList();
        } else {
            Collection<TwitchMessage> trimmedMessageList = messageList
                    .stream()
                    .limit(messageLookBehind)
                    .collect(Collectors.toList());
            BlacklistEntry blacklistEntry = m_blacklistManager.addToBlacklist(input, blacklistType);
            return trimmedMessageList
                    .stream()
                    .filter(message -> blacklistEntry.matches(message.getMessage()))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Remove exact blacklist entry
     * @return Blacklist entry that has been removed
     * @throws ChannelOperationException if Blacklist entry request was not found.
     */
    public BlacklistEntry removeBlacklistItem(String input, BlacklistType blacklistType) throws ChannelOperationException {
        try {
            return m_blacklistManager.removeFromBlacklist(input, blacklistType);
        } catch (BlacklistOperationException e) {
            throw new ChannelOperationException("Failed to remove blacklist entry " + input + " of type " +
                                                blacklistType.toString());
        }
    }

    /**
     * Fuzzy removal of blacklist entry. Will first search exact, then any matching entry
     * @param input contents of blacklist message
     * @return All blacklist entries that have been removed.
     */
    Collection<BlacklistEntry> removeBlacklistItem(String input) {
        return m_blacklistManager.removeFromBlacklist(input);
    }

    public String getChannelName() {
        return m_channelName;
    }
<<<<<<< f9ac3a962a7c90330d9d44adce2245674b350b67
=======
  }

  /**
   * Fuzzy removal of blacklist entry. Will first search exact, then any matching entry
   *
   * @param input contents of blacklist message
   * @return All blacklist entries that have been removed.
   */
  public Collection<BlacklistEntry> removeBlacklistItem(String input) {
    return blacklistManager.removeFromBlacklist(input);
  }

  public String getChannelName() {
    return channelName;
  }
>>>>>>> Connected Blacklist Executor
}
