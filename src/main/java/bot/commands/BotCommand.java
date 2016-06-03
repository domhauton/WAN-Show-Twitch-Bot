package bot.commands;

import bot.commands.executors.BotCommandUtil;
import twitch.chat.data.OutboundTwitchMessage;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
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

    private String m_commandName;
    private Set<Character> m_flags;
    private List<String> m_args;

    public BotCommand(String inputMessage) {
        boolean isValidCommand = inputMessage.startsWith(s_commandPrefix);
        if(isValidCommand){
            String command = inputMessage.replaceFirst(s_commandPrefix, "");
            parseCommandMessage(command);
        } else {
            throw new IllegalArgumentException("Command Invalid");
        }
    }

    private void parseCommandMessage(String botCommand) {
        String[] splitCommands = getChunks(botCommand);
        m_commandName = splitCommands[0];
        m_flags = Arrays.asList(splitCommands).stream()
                .filter(command -> command.startsWith(String.valueOf(s_flagPrefix)))
                .map(flags -> flags.substring(1))
                .map(String::toLowerCase)
                .map(CharSequence::chars)
                .flatMap(intStream -> intStream.mapToObj(i -> (char) i))
                .collect(Collectors.toSet());
        m_args = Stream.of(splitCommands)
                .filter(command -> !command.startsWith(String.valueOf(s_flagPrefix)))
                .collect(Collectors.toList());
    }

    private String[] getChunks(String inputString){
        return inputString.split(s_chunkSeparator + "(?=([^" + s_escapeChar + "]*" + s_escapeChar + "[^" +
                                s_escapeChar +
                           "]*" + s_escapeChar + ")*[^" + s_escapeChar + "]*$)");
    }

    public Collection<OutboundTwitchMessage> runCommand() throws BotCommandException {
        TwitchCommand twitchCommand = TwitchCommand.getCommand(m_commandName);

        return BotCommandUtil.parseCommand(twitchCommand, m_flags, m_args);
    }
}
