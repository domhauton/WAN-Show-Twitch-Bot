package bot.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import twitch.channel.ChannelManager;
import twitch.channel.ChannelOperationException;
import twitch.channel.TwitchUser;
import twitch.channel.permissions.UserPermission;
import twitch.chat.data.OutboundTwitchMessage;
import twitch.chat.data.OutboundTwitchWhisper;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Dominic Hauton on 06/05/2016.
 *
 * Converts an input String into a command.
 */
public class BotCommand {
    private final static String s_commandPrefix = "!bot ";
    private final static char s_escapeChar = '"';
    private final static char s_chunkSeparator = ' ';
    private final static char s_flagPrefix = '-';

    private final TwitchUser m_twitchUser;
    private final ChannelManager m_channelManager;

    private BotCommandType m_botCommandType;
    private ImmutableSet<Character> m_flags;
    private ImmutableList<String> m_args;


    public BotCommand(String inputMessage, TwitchUser twitchUser, ChannelManager channelManager) {
        m_twitchUser = twitchUser;
        m_channelManager = channelManager;
        String command = inputMessage.replaceFirst(s_commandPrefix, "");
        parseCommandMessage(command);
    }

    /**
     * Called during constructor
     */
    private void parseCommandMessage(String rawBotCommand) {
        String[] splitCommands = getChunks(rawBotCommand);
        if(splitCommands.length > 0) {
            String commandName = splitCommands[0];
            m_botCommandType = BotCommandType.getCommand(commandName);
            m_flags = Arrays.asList(splitCommands).stream()
                    .filter(command -> command.startsWith(String.valueOf(s_flagPrefix)))
                    .map(flags -> flags.substring(1))
                    .map(String::toLowerCase)
                    .map(CharSequence::chars)
                    .flatMap(intStream -> intStream.mapToObj(i -> (char) i))
                    .collect(Collectors.collectingAndThen(Collectors.toSet(), ImmutableSet::copyOf));
            m_args = Stream.of(splitCommands)
                    .filter(command -> !command.startsWith(String.valueOf(s_flagPrefix)))
                    .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
        } else {
            m_botCommandType = BotCommandType.UNKNOWN;
            m_flags = ImmutableSet.of();
            m_args = ImmutableList.of();
        }
    }

    /**
     * Separates command into chunks respecting escape chars.
     */
    private String[] getChunks(String inputString){
        return inputString.split(s_chunkSeparator + "(?=([^" + s_escapeChar + "]*" + s_escapeChar + "[^" +
                                s_escapeChar +
                           "]*" + s_escapeChar + ")*[^" + s_escapeChar + "]*$)");
    }

    public Collection<OutboundTwitchMessage> parseCommand() throws BotCommandException {
        UserPermission userPermission;
        try {
            userPermission = m_channelManager.getPermission(m_twitchUser);
        } catch (ChannelOperationException e) {
            userPermission = UserPermission.ChannelUser;
        }
        if(userPermission.authorizedForActionOfPermissionLevel(m_botCommandType.requiredUserPermissionLevel())) {
            return m_botCommandType.getCommandExecutor().executeCommand(m_flags, m_args, m_channelManager);
        } else {
            throw new BotCommandException("Insufficient permissions to run command.");
        }
    }

    public static boolean isValidCommand(String rawInputMessage) {
        return rawInputMessage.startsWith(s_commandPrefix);
    }
}
