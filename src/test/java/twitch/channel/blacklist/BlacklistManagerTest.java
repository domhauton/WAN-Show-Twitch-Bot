package twitch.channel.blacklist;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Dominic Hauton on 13/07/2016.
 */
public class BlacklistManagerTest {
    private BlacklistManager blacklistManager;

    @Before
    public void setUp() throws Exception {
        blacklistManager = new BlacklistManager();
    }

    @Test
    public void addToBlacklist() throws Exception {
        BlacklistEntry blacklistEntry = blacklistManager.addToBlacklist("foobar", BlacklistType.MESSAGE);
    }

    @Test
    public void addToBlacklist1() throws Exception {

    }

    @Test
    public void removeFromBlacklist() throws Exception {

    }

}