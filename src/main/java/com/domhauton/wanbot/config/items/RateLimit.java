package com.domhauton.wanbot.config.items;

import java.util.Objects;

public class RateLimit {
  // Defaults are rough minimums to avoid bans
  private int windowSizeSecs = 30;
  private int eventsPerWindow = 49;

  private RateLimit() {
    // Jackson ONLY
  }

  RateLimit(int windowSizeSecs, int eventsPerWindow) {
    this.windowSizeSecs = windowSizeSecs;
    this.eventsPerWindow = eventsPerWindow;
  }

  public int getWindowSizeSecs() {
    return windowSizeSecs;
  }

  public int getEventsPerWindow() {
    return eventsPerWindow;
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RateLimit rateLimit = (RateLimit) o;
    return getWindowSizeSecs() == rateLimit.getWindowSizeSecs() &&
        getEventsPerWindow() == rateLimit.getEventsPerWindow();
  }

  @Override
  public int hashCode() {

    return Objects.hash(getWindowSizeSecs(), getEventsPerWindow());
  }
}
