package bot.commands;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Dominic Hauton on 06/05/2016.
 */
public class BotCommandMessage {
    private final static String s_commandPrefix = "!bot ";
    private final static char s_escapeChar = '`';
    private final static char s_chunkSeparator = ' ';

    private String m_commandName;

    private List<String> m_options;

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

    }

    private String[] getChunks(String inputString){
        return inputString.split(s_chunkSeparator + "(?=([^" + s_escapeChar + "]*" + s_escapeChar + "[^" +
                                s_escapeChar +
                           "]*" + s_escapeChar + ")*[^" + s_escapeChar + "]*$)");
    }
}
