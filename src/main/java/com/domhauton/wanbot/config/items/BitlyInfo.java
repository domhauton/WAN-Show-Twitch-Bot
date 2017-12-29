package com.domhauton.wanbot.config.items;

import java.util.Objects;

/**
 * Created by dominic on 27/05/17.
 */
public class BitlyInfo {
  private boolean enabled = false;

  public BitlyInfo() {
    // Jackson ONLY
  }

  public BitlyInfo(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BitlyInfo bitlyInfo = (BitlyInfo) o;
    return isEnabled() == bitlyInfo.isEnabled();
  }

  @Override
  public int hashCode() {

    return Objects.hash(isEnabled());
  }
}
