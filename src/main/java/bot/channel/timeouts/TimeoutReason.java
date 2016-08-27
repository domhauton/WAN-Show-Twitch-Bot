package bot.channel.timeouts;


import org.joda.time.Duration;

/**
 * Created by Dominic Hauton on 22/08/2016.
 *
 * A list of possible offenses
 */
public enum TimeoutReason {
  MESSAGE_REPETITION("You have been timed out for repeating the same message.", Duration.standardSeconds(20), 0.5f),
  CHAT_REPETITION("You have been timed out. Your MESSAGE has been posted in the chat recently.", Duration
      .standardSeconds(20), 0.5f),
  MESSAGE_RATE("You have been timed out for posting messages to quickly.", Duration.standardSeconds
      (40), 2.0f),
  EXCESSIVE_SYMBOLS("You have been timed out for posting ASCII art.", Duration.standardSeconds
      (20), 2.0f),
  BLACKLISTED_WORD("Your message contained a blacklisted word or phrase.", Duration.standardSeconds(60), 0.5f);

  private String message;
  private Duration timeout;
  private Float multiplier;

  TimeoutReason(String message, Duration timeout, Float multiplier) {
    this.message = message;
    this.timeout = timeout;
    this.multiplier = multiplier;
  }

  public String getMessage() {
    return message;
  }

  public Duration getTimeout() {
    return timeout;
  }

  public Float getMultiplier() {
    return multiplier;
  }
}
