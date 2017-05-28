package com.domhauton.wanbot.bot.channel.settings;

import com.domhauton.wanbot.bot.channel.ChannelOperationException;

/**
 * Created by Dominic Hauton on 26/06/2016.
 *
 * Basic Exception for the DAO
 */
public class ChannelSettingDAOException extends ChannelOperationException {

  public ChannelSettingDAOException(String message) {
    super(message);
  }
}
