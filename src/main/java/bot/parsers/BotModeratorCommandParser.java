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
            OutboundTwitchMessage outboundTwitchMessage =
                    new OutboundTwitchMessage(responsePayload, channelName);
            return new Arrays.asList(outboundTwitchMessage);
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
            if(word.startsWith("blw")){
                return bLWord(word.substring(4), channel);
            } else if(word.startsWith("rmblw")){
                return removeBLWord(word.substring(6));
            } else if(word.startsWith("blm")){
                return bLMsg(word.substring(4), channel);
            } else if(word.startsWith("rmblm")){
                return removeBLMsg(word.substring(6));
            } else if(word.startsWith("addop")){
                return addOperator(word.substring(6));
            } else if(word.startsWith("rmop")){
                return rmOperator(word.substring(5));
            } else if(word.startsWith("sstart")){
                return setStartTime();
            }
        }
        return "Unknown Command Entered.";
    }

    /**
     * Takes a list of messages and rule. Any messages breaching rule are returned.
     * @return Messages breaching given rule.
     */
    private static Collection<TwitchMessage> findMessagesBreachingRule(
            BlacklistEntry blacklistEntry,
            ImmutableTwitchMessageList messageList){
        return messageList.stream()
                .filter(twitchMessage ->  blacklistEntry.matches(twitchMessage.getMessage()))
                .collect(Collectors.toList());
    }

    /**
     * Adds a word to the blacklist.
     * @param word Word to add to blacklist
     * @return response message
     */
    private String bLWord(String word, String channel){
        if(word.length()<3) return "Word not long enough.";
        if(blockedWords.contains(lowerCaseWord))
            return lowerCaseWord + " already on blacklist.";
        blockedWords.add(lowerCaseWord);
        channelManager.getMessageSnapshot()
                .stream()
                .filter(message -> !channelManager.checkPermission(message.getTwitchUser(), UserPermission.ChannelModerator))
                .filter(message -> message.getMessage().toLowerCase().contains(lowerCaseWord))
                .forEach(message -> timeoutUser(message.getTwitchUser(),
                        channel,
                        Period.seconds(45),
                        "A word you have recently used has been blacklisted"));
        return lowerCaseWord + " added to blacklist. Previous messages breaching rule this will be banned.";
    }

    /**
     * Removes a word from the blacklist if possible. If not possible it is ignored.
     * @param word word to remove.
     * @return response message.
     */
    private String removeBLWord(String word){
        if(blockedWords.contains(word)){
            blockedWords.remove(word);
            return word + " removed from the blacklist.";
        }
        return word + " not found on the blacklist";
    }

    /**
     * Adds a word to the blacklist.
     * @param word Word to add to blacklist
     * @return response word
     */
    private String bLMsg(String word, String channel){
        String lowerCaseMessage = word.toLowerCase();
        if(blockedMessages.contains(lowerCaseMessage))
            return lowerCaseMessage + " already on blacklist.";
        blockedMessages.add(lowerCaseMessage);
        channelManager.getMessageSnapshot()
                .stream()
                .filter(message -> !channelManager.checkPermission(message.getTwitchUser(), UserPermission.ChannelModerator))
                .filter(message -> message.getMessage().equalsIgnoreCase(lowerCaseMessage))
                .forEach(message -> timeoutUser(message.getTwitchUser(),
                        channel,
                        Period.seconds(45),
                        "A message you recently sent has been blacklisted"));
        return lowerCaseMessage + " added to message blacklist. Previous messages breaching this rule will be banned.";
    }
    /**
     * Removes a word from the blacklist if possible. If not possible it is ignored.
     * @param word word to remove.
     * @return response word.
     */
    private String removeBLMsg(String word){
        if(blockedWords.contains(word)){
            blockedWords.remove(word);
            return word + " removed from the blacklist.";
        }
        return word + " not found on the blacklist";
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
