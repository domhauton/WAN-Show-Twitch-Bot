package channel;

import channel.users.TwitchUserManager;

/**
 * Created by Dominic Hauton on 12/03/2016.
 */
public class ChannelManager {
    private TwitchUserManager       twitchUserManager;
    private MessageManager          messageManager;
    private TwitchConnectionPool    twitchConnectionPool;
    private ChannelAnnouncer        channelAnnouncer;

    public ChannelManager() {
        twitchUserManager =     new TwitchUserManager();
        messageManager =        new MessageManager();
        twitchConnectionPool =  new TwitchConnectionPool();
        channelAnnouncer =      new ChannelAnnouncer();
    }

    public ChannelManager(TwitchUserManager twitchUserManager,
                          MessageManager messageManager,
                          TwitchConnectionPool twitchConnectionPool,
                          ChannelAnnouncer channelAnnouncer) {
        this.twitchUserManager  = twitchUserManager;
        this.messageManager     = messageManager;
        this.twitchConnectionPool = twitchConnectionPool;
        this.channelAnnouncer   = channelAnnouncer;
    }

    public TwitchUserManager getTwitchUserManager() {
        return twitchUserManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public TwitchConnectionPool getTwitchConnectionPool() {
        return twitchConnectionPool;
    }

    public ChannelAnnouncer getChannelAnnouncer() {
        return channelAnnouncer;
    }
}
