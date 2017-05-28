package com.domhauton.wanbot.config;

/**
 * Created by dominic on 27/05/17.
 */
public class ChatRate {
  private String messages;
  private String timeUnit;

  private ChatRate() {
  } // Jackson ONLY

  public ChatRate(String messages, String timeUnit) {
    this.messages = messages;
    this.timeUnit = timeUnit;
  }

  public String getMessages() {
    return messages;
  }

  public String getTimeUnit() {
    return timeUnit;
  }
}
