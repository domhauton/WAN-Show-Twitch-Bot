package irc.info;

/**
 * Created by Dominic H on 09/08/2015 at 17:14.
 *
 * Stores Server Information for DI
 */
public class ServerInfo {
    private String serverIP;
    private Integer port;
    private String channelName;
    private Integer messageRate;
    private Integer burstPeriod;
    private LoginInfo loginInfo;

    public ServerInfo(String serverIP, Integer port, String channelName, Integer messageRate, Integer burstPeriod, LoginInfo loginInfo) {
        this.serverIP = serverIP;
        this.port = port;
        this.channelName = channelName;
        this.messageRate = messageRate;
        this.burstPeriod = burstPeriod;
        this.loginInfo = loginInfo;
    }

    public String getServerIP() {
        return serverIP;
    }

    public Integer getPort() {
        return port;
    }

    public String getChannelName() {
        return channelName;
    }

    public Integer getMessageRate() {
        return messageRate;
    }

    public Integer getBurstPeriod() {
        return burstPeriod;
    }

    public LoginInfo getLoginInfo() {
        return loginInfo;
    }
}
