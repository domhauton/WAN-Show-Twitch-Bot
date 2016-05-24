package bot.commands;

import twitch.channel.data.TwitchMessage;
import twitch.chat.data.OutboundTwitchMessage;

import java.util.Collection;
import java.util.function.Function;

/**
 * Created by Dominic Hauton on 23/05/2016.
 */
public enum TwitchCommand {
    blacklist,
    bl;

    private Function<BotCommandMessage, Collection<TwitchMessage>> commandAction;

    TwitchCommand(Function<BotCommandMessage, Collection<OutboundTwitchMessage>> commandAction) {
        this.commandAction = commandAction;
    }

    public Collection<OutboundTwitchMessage> runCommand(BotCommandMessage botCommandMessage) {
        return commandAction.apply(botCommandMessage);
    }

    public char[] getValidFlagList() {
        return new char[0]; //FIXME Complete
    }

    public String getManualPage() {
        return ""; //FIXME Complete
    }

    public String
}
