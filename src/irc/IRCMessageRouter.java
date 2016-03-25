package irc;

import com.google.inject.Inject;
import irc.pools.PrivateMessagePool;
import irc.pools.PublicMessagePool;

import javax.inject.Singleton;

/**
 * Created by Dominic Hauton on 12/03/2016.
 */
@Singleton
public class IRCMessageRouter {
    private PrivateMessagePool privateMessagePool;
    private PublicMessagePool publicMessagePool;

    @Inject
    public IRCMessageRouter(PrivateMessagePool privateMessagePool, PublicMessagePool publicMessagePool) {
        this.privateMessagePool = privateMessagePool;
        this.publicMessagePool = publicMessagePool;
    }

    public void sendPrivateMessage() {

    }
}
