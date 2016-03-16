package irc.info;

/**
 * Created by Dominic H on 09/08/2015 at 17:07.
 *
 * Stores login info for DI
 */
public class LoginInfo {
    String username;
    String password;

    public LoginInfo(String username, String password){
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
