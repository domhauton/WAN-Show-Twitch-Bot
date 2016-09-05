package bot.channel.url;

import bot.channel.ChannelOperationException;

/**
 * Created by Dominic Hauton on 05/09/2016.
 *
 * Custom exception for invalid URL
 */
public class URLInvalidException extends ChannelOperationException {
  /**
   * Constructs a new exception with the specified detail MESSAGE.  The
   * cause is not initialized, and may subsequently be initialized by
   * a call to {@link #initCause}.
   *
   * @param message the detail MESSAGE. The detail MESSAGE is saved for later retrieval by the
   *                {@link #getMessage()} method.
   */
  public URLInvalidException(String message) {
    super(message);
  }
}
