package com.domhauton.wanbot.url;

/**
 * Created by Dominic Hauton on 05/09/2016.
 *
 * An interface for link modifications
 */
public interface URLConverter {
  /**
   *
   * @param inputLink
   * @return
   */
  String convertLink(String inputLink) throws URLInvalidException;
}
