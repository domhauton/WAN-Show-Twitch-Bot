package bot.channel;

/**
 * Created by Dominic Hauton on 23/05/2016.
 *
 * Standard Exception class
 */
public class ChannelOperationException extends Exception {

  /**
   * Constructs a new exception with the specified detail MESSAGE.  The
   * cause is not initialized, and may subsequently be initialized by
   * a call to {@link #initCause}.
   *
   * @param message the detail MESSAGE. The detail MESSAGE is saved for later retrieval by the
   *                {@link #getMessage()} method.
   */
  public ChannelOperationException(String message) {
    super(message);
  }
}
