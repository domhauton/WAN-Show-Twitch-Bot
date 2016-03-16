package message.controllers;

import message.InboundMessage;

import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Dominic H on 09/08/2015 at 22:54.
 *
 * Given to a Listener to route incoming messages.
 */
public class InboundRouter {

    Consumer<InboundMessage> onMessage;
    Consumer<InboundMessage> onAction;

    public InboundRouter(Consumer<InboundMessage> onMessage, Consumer<InboundMessage> onAction) {
        this.onMessage = onMessage;
        this.onAction = onAction;
    }

    public void routeMessage(String sender, String message){
        InboundMessage inboundMessage = new InboundMessage(message, sender);
        inboundMessage.enrichMessage();
        onMessage.accept(inboundMessage);
    }

    public void routeAction(String sender, String message){
        InboundMessage inboundMessage = new InboundMessage(message, sender);
        inboundMessage.enrichMessage();
        onAction.accept(inboundMessage);
    }

    public void userList(String channel, Set<String> userSet){
        //TODO Do something on receiving userList
        Logger.getGlobal().log(Level.INFO, "User List not implemented yet.");
    }
}
