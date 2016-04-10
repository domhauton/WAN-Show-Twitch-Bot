package twitchchat.irc;

import org.jibble.pircbot.PircBot;

/**
 * Created by Dominic Hauton on 10/04/2016.
 *
 * Extends PircBot to allow instantiation.
 */
class PircBotImpl extends PircBot {
    PircBotImpl(String name) {
        super();
        setName(name);
    }
}
