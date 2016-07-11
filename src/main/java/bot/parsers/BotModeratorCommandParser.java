package bot.parsers;

import org.joda.time.DateTime;
import org.joda.time.Period;
import twitch.channel.blacklist.BlacklistEntry;
import twitch.channel.message.TwitchMessage;
import twitch.channel.TwitchUser;
import twitch.channel.message.ImmutableTwitchMessageList;
import twitch.channel.permissions.UserPermission;
import twitch.chat.data.OutboundTwitchMessage;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Created by Dominic Hauton on 03/05/2016.
 *
 * Parses an inbound message for operator commands
 */
public class BotModeratorCommandParser implements TwitchMessageParser{

    private static final String s_commandKeyWord = "!bot";

    /**
     * Returns a collection of responses to the given InboundMessage
     * @return Responses for message.
     */
    @Override
    public Collection<OutboundTwitchMessage> parseMessage(TwitchMessage inboundTwitchMessage) {
        boolean isBotCommand = inboundTwitchMessage.getSimpleMessagePayload().startsWith(s_commandKeyWord);
        if(isBotCommand) {
            String responsePayload = botCommand(inboundTwitchMessage.getMessage(), inboundTwitchMessage.getTwitchChannel());
            String channelName = inboundTwitchMessage.getTwitchChannel();
            OutboundTwitchMessage outboundTwitchMessage = new OutboundTwitchMessage(responsePayload, channelName);
            return Collections.singletonList(outboundTwitchMessage);
        }
        return Collections.emptyList();
    }

    /**
     * @return true if message should be sent through parser.
     */
    @Override
    public boolean isParsingRequired(UserPermission userPermission) {
        return userPermission.hasRequiredPermissions(UserPermission.BotModerator);
    }

    /**
     * Used to add a word to the blacklist
     */
    private String botCommand(String word, String channel) {
        if(word.startsWith(s_commandKeyWord)){
            word = word.substring(s_commandKeyWord.length()+1); //remove the !bot section.
            if(word.startsWith("addop")){
                return addOperator(word.substring(6));
            } else if(word.startsWith("rmop")){
                return rmOperator(word.substring(5));
            } else if(word.startsWith("sstart")){
                return setStartTime();
            }
        }
        return "Unknown Command Entered.";
    }

    private String addOperator(String command){
        String[] splitCommand = command.split(" ");
        if (splitCommand.length != 2) return "Syntax Error.";
        try{
            String tier = splitCommand[0];
            String username = splitCommand[1];
            UserPermission userPermission = UserPermission.valueOf(tier);
            channelManager.setPermission(new TwitchUser(username), userPermission);
            return String.format("Added %s to %s", username, userPermission);
        } catch(Exception e){
            return "Syntax Error.";
        }
    }

    private String rmOperator(String name){
        channelManager.setPermission(new TwitchUser(name), UserPermission.getDefaultPermission());
        return name + " is no longer an operator.";
    }

    private String setStartTime(){
        streamStartTime = DateTime.now();
        return "Show Start time has been set.";
    }
}
