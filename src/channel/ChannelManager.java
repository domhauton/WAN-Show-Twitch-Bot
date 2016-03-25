package channel;

import channel.message.MessageManager;
import channel.users.TwitchUserManager;

/**
 * Created by Dominic Hauton on 12/03/2016.
 */
public class ChannelManager {
    private TwitchUserManager       twitchUserManager;
    private MessageManager          messageManager;
    private ChannelAnnouncer        channelAnnouncer;

    public ChannelManager() {
        twitchUserManager =     new TwitchUserManager();
        messageManager =        new MessageManager();
        channelAnnouncer =      new ChannelAnnouncer();
    }

    public TwitchUserManager getTwitchUserManager() {
        return twitchUserManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }
}
