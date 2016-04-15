package twitch.chat.data;

import org.joda.time.Period;

/**
 * Created by Dominic Hauton on 15/04/2016.
 *
 * Extends an OutboundTwitchMessage to implement a Timeout
 */
public class OutboundTwitchTimeout extends OutboundTwitchMessage {
    public OutboundTwitchTimeout(String targetChannel, String targetUser, Period timeoutPeriod) {
        super(createBanMessage(timeoutPeriod, targetUser), targetChannel);
    }

    private static String createBanMessage(Period timeoutPeriod, String targetUser) {
        Integer seconds = timeoutPeriod.toStandardSeconds().getSeconds();
        return ".timeout " + targetUser + " " + seconds.toString();
    }
}
