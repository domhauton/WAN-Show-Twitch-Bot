package bot.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Dominic Hauton on 06/05/2016.
 *
 * Converts an input String into a command.
 */
public class BotCommandMessage {
    private final static String s_commandPrefix = "!bot ";
    private final static char s_escapeChar = '"';
    private final static char s_chunkSeparator = ' ';
    private final static char s_flagPrefix = '-';

    private String m_commandName;
    private Set<Character> m_flags;
    private List<String> m_args;

    public BotCommandMessage(String inputMessage) {
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

    public String getCommandName() {
        return m_commandName;
    }

    public boolean containsFlag(Character flag) {
        return m_flags.contains(flag);
    }

    public List<String> getArgs() {
        return m_args;
    }
}
