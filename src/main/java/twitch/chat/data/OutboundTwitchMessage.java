package twitch.chat.data;

/**
 * Created by Dominic Hauton on 09/04/2016.
 *
 * Immutable Data class for OutboundMessages
 */
public class OutboundTwitchMessage {
    private final String payload;
    private final String target;

    public OutboundTwitchMessage(String payload, String target) {
        this.payload = payload;
        this.target = target;
    }

    public String getPayload() {
        return payload;
    }

    /**
     * Returns a new outbound MESSAGE with the given payload
     */
    public OutboundTwitchMessage setPayload(String payload) {
        return new OutboundTwitchMessage(payload, target);
    }

    public String getTarget() {
        return target;
    }

    /**
     * Returns a new outbound MESSAGE with the given payload
     */
    public OutboundTwitchMessage setTarget(String target) {
        return new OutboundTwitchMessage(payload, target);
    }

    @Override
    public String toString() {
        return "OutboundTwitchMessage{" +
                target +
                ":\t" +
                payload;
    }
}
