package com.domhauton.wanbot.bot.channel;

import com.domhauton.wanbot.bot.channel.blacklist.BlacklistEntry;
import com.domhauton.wanbot.bot.channel.blacklist.BlacklistManager;
import com.domhauton.wanbot.bot.channel.blacklist.BlacklistOperationException;
import com.domhauton.wanbot.bot.channel.blacklist.BlacklistType;
import com.domhauton.wanbot.bot.channel.message.ImmutableTwitchMessageList;
import com.domhauton.wanbot.bot.channel.message.MessageManager;
import com.domhauton.wanbot.bot.channel.message.TwitchMessage;
import com.domhauton.wanbot.bot.channel.permissions.PermissionException;
import com.domhauton.wanbot.bot.channel.permissions.PermissionsManager;
import com.domhauton.wanbot.bot.channel.permissions.UserPermission;
import com.domhauton.wanbot.bot.channel.settings.ChannelSettingDAOHashMapImpl;
import com.domhauton.wanbot.bot.channel.settings.ChannelSettingDao;
import com.domhauton.wanbot.bot.channel.settings.enums.ChannelSettingInteger;
import com.domhauton.wanbot.bot.channel.settings.enums.ChannelSettingString;
import com.domhauton.wanbot.bot.channel.settings.enums.IChannelSetting;
import com.domhauton.wanbot.bot.channel.timeouts.TimeoutManager;
import com.domhauton.wanbot.bot.channel.timeouts.TimeoutReason;
import com.domhauton.wanbot.url.URLConverter;
import com.domhauton.wanbot.url.URLConverterImpl;
import com.domhauton.wanbot.url.URLInvalidException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Duration;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Created by Dominic Hauton on 12/03/2016.
 *
 * Stores information about the user channel.
 */
public class ChannelManager {
  private static final Logger log = LogManager.getLogger();
  private final String channelName;
  private final PermissionsManager permissionsManager;
  private final MessageManager messageManager;
  private final TimeoutManager timeoutManager;
  private final BlacklistManager blacklistManager;
  private final ChannelSettingDao channelSettingDao;
  private final URLConverter urlConverter;

  public ChannelManager(String channelName) {
    this(channelName,
        new PermissionsManager(),
        new MessageManager(),
        new TimeoutManager(),
        new BlacklistManager(),
        new ChannelSettingDAOHashMapImpl(),
        new URLConverterImpl());
  }

  ChannelManager(
      String channelName,
      PermissionsManager permissionsManager,
      MessageManager messageManager,
      TimeoutManager timeoutManager,
      BlacklistManager blacklistManager,
      ChannelSettingDao channelSettingDao,
      URLConverter urlConverter) {
    this.channelName = channelName;
    this.permissionsManager = permissionsManager;
    this.messageManager = messageManager;
    this.timeoutManager = timeoutManager;
    this.blacklistManager = blacklistManager;
    this.channelSettingDao = channelSettingDao;
    this.urlConverter = urlConverter;
  }

  /**
   * Checks if the given user has permission for the requested action per
   *
   * @return true if user has permission for the action
   */
  boolean checkPermission(TwitchUser user, UserPermission requiredPermission) throws ChannelOperationException {
    return getPermission(user).authorizedForActionOfPermissionLevel(requiredPermission);
  }

  public UserPermission getPermission(TwitchUser twitchUser) throws ChannelOperationException {
    try {
      return permissionsManager.getUser(twitchUser);
    } catch (PermissionException e) {
      String defaultPermissionString = channelSettingDao.getSettingOrDefault(channelName, ChannelSettingString
          .DEFAULT_PERMISSION);
      try {
        return UserPermission.valueOf(defaultPermissionString);
      } catch (IllegalArgumentException e2) {
        throw new ChannelOperationException("Failed to cast default permission to valid permission");
      }
    }
  }

  public void setPermission(TwitchUser twitchUser, UserPermission newPermission) {
    log.info("Setting permission {} for user {}", newPermission::toString, twitchUser::toString);
    permissionsManager.changeUserPermission(twitchUser, newPermission);
  }

  /**
   * Adds a message to the channel message manager.
   *
   * @return true if message passed blacklists.
   * @throws ChannelOperationException insertion failed. Reason unknown.
   */
  public boolean addChannelMessage(TwitchMessage message) throws ChannelOperationException {
    if (messageManager.addMessage(message)) {
      // n.b. Inverted boolean!
      return !blacklistManager.isMessageBlacklisted(message.getMessage());
    } else {
      throw new ChannelOperationException("Failed to insert message into channel. Reason Unknown.");
    }
  }

  public ImmutableTwitchMessageList getMessageSnapshot() {
    return messageManager.getChannelSnapshot();
  }

  public ImmutableTwitchMessageList getMessageSnapshot(TwitchUser username) {
    return messageManager.getUserSnapshot(username);
  }

  Duration getUserTimeout(TwitchUser twitchUser) {
    return timeoutManager.getUserTimeout(twitchUser.getUsername());
  }

  public Duration addUserTimeout(String twitchUser, TimeoutReason timeoutReason) {
    log.info("Adding a timeout {} for user {}", timeoutReason::toString, twitchUser::toString);
    return timeoutManager.addUserTimeout(twitchUser, timeoutReason);
  }

  public Collection<TwitchMessage> blacklistItem(String input, BlacklistType blacklistType) throws
      ChannelOperationException {
    Integer messageLookBehind = channelSettingDao.getSettingOrDefault(channelName, ChannelSettingInteger
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
    log.info("Adding item {} to channel {} blacklist as {} with {} look behind", input, channelName,
        blacklistType, messageLookBehind);
    ImmutableTwitchMessageList messageList = getMessageSnapshot();
    if (messageLookBehind <= 0) {
      blacklistManager.addToBlacklist(input, blacklistType);
      return Collections.emptyList();
    } else {
      Collection<TwitchMessage> trimmedMessageList = messageList
          .stream()
          .limit(messageLookBehind)
          .collect(Collectors.toList());
      BlacklistEntry blacklistEntry = blacklistManager.addToBlacklist(input, blacklistType);
      return trimmedMessageList
          .stream()
          .filter(message -> blacklistEntry.matches(message.getMessage()))
          .collect(Collectors.toList());
    }
  }

  /**
   * Remove exact blacklist entry
   *
   * @return Blacklist entry that has been removed
   * @throws ChannelOperationException if Blacklist entry request was not found.
   */
  public BlacklistEntry removeBlacklistItem(String input, BlacklistType blacklistType) throws ChannelOperationException {
    try {
      return blacklistManager.removeFromBlacklist(input, blacklistType);
    } catch (BlacklistOperationException e) {
      throw new ChannelOperationException("Failed to remove blacklist entry " + input + " of type " +
          blacklistType.toString());
    }
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

  public String customiseURL(String link) throws ChannelOperationException {
    try {
      return urlConverter.convertLink(link);
    } catch (URLInvalidException e) {
      throw new ChannelOperationException(e.getMessage());
    }
  }

  public <T> T getChannelSetting(IChannelSetting<T> channelSetting) {
    return channelSettingDao.getSettingOrDefault(getChannelName(), channelSetting);
  }
}
