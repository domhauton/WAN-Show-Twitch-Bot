package channel.users;

import com.google.common.collect.ImmutableList;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Dominic Hauton on 12/03/2016.
 */
public class TwitchUserManager {
    private HashMap<String, TwitchUser> userHashMap;
    private HashMap<UserPermission, ImmutableList> userSnapshot;

    public TwitchUserManager() {
        userHashMap = new HashMap<>();
    }

    public TwitchUser getUser(String username) {
        return userHashMap.putIfAbsent(username, new TwitchUser(username, UserPermission.ChannelUser) );
    }

    /**
     * Get a list of all the users at the given permission level.
     * @param permission
     * @return
     */
    public ImmutableList getUserSnapshot(UserPermission permission) {
        List<TwitchUser> filteredUserList = userHashMap.values().stream()
                .filter(user -> user.getUserPermission().equals(permission))
                .collect(Collectors.toList());
        return ImmutableList.copyOf( filteredUserList );
    }

    public TwitchUser addUser(TwitchUser twitchUser){
        return userHashMap.put(twitchUser.getUsername(), twitchUser);
    }
}
