package channel.users;

import java.util.HashMap;

/**
 * Created by Dominic Hauton on 12/03/2016.
 */
public class TwitchUserManager {
    private HashMap<String, TwitchUser> userHashMap;

    public TwitchUserManager() {
        userHashMap = new HashMap<>();
    }

    public TwitchUser getUser(String username) {
        return userHashMap.computeIfAbsent(username, name -> new TwitchUser(name, UserPermission.ChannelUser));
    }

    public TwitchUser addUser(TwitchUser twitchUser){
        return userHashMap.put(twitchUser.getUsername(), twitchUser);
    }
}
