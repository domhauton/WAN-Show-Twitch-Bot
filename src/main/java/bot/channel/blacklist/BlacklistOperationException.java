package bot.channel.blacklist;

import bot.channel.ChannelOperationException;

/**
 * Created by Dominic Hauton on 23/05/2016.
 *
 * Exception to throw when trying to add an existing blacklist entry.
 */
public class BlacklistOperationException extends ChannelOperationException {

  BlacklistOperationException(String message) {
    super(message);
  }
}
