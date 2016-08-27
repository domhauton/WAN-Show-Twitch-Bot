package bot.channel.permissions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import bot.channel.TwitchUser;

/**
 * Created by Dominic Hauton on 12/03/2016.
 *
 * Contains the permissions for all of the users.
 */
public class PermissionsManager {
  private static final Logger log = LogManager.getLogger();
  private ConcurrentHashMap<TwitchUser, UserPermission> userHashMap;

  public PermissionsManager() {
    userHashMap = new ConcurrentHashMap<>();
  }

  /**
   * Gets the current permissions for the given user.
   *
   * @return The stored permissions.
   * @throws PermissionException if permission not stored.
   */
  public UserPermission getUser(TwitchUser user) throws PermissionException {
    log.debug("Retrieving permissions for user {} from Permissions Manager.", user::getUsername);
    UserPermission permission = userHashMap.get(user);
    if (Objects.nonNull(permission)) {
      return permission;
    } else {
      log.info("Permission for user {} not found.", user::getUsername);
      throw new PermissionException("Permission for user " + user.getUsername() + " not found.");
    }
  }

  /**
   * Adds/Replaces any user stored
   */
  public void changeUserPermission(TwitchUser user, UserPermission userPermission) {
    log.info("Changing permission of user {} to {}", user::getUsername, userPermission::toString);
    userHashMap.put(user, userPermission);
  }
}
