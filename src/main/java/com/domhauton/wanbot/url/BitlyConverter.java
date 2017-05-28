package com.domhauton.wanbot.url;

import com.rosaloves.bitlyj.Bitly;

/**
 * Created by dominic on 27/05/17.
 */
public class BitlyConverter implements URLConverter {
  private final Bitly.Provider bitlyProvider;

  public BitlyConverter(String username, String token) {
    bitlyProvider = Bitly.as(username, token);
  }

  @Override
  public String convertLink(String inputLink) throws URLInvalidException {
    return bitlyProvider.call(Bitly.shorten(inputLink)).getShortUrl();
  }
}
