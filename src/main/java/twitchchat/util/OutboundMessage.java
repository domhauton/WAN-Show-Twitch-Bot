package twitchchat.util;

/**
 * Created by Dominic Hauton on 09/04/2016.
 *
 * Immutable Data class for OutboundMessages
 */
public class OutboundMessage {
    private final String payload;
    private final String target;

    public OutboundMessage(String payload, String target) {
        this.payload = payload;
        this.target = target;
    }

    public String getPayload() {
        return payload;
    }

    /**
     * Returns a new outbound message with the given payload
     */
    public OutboundMessage setPayload(String payload) {
        return new OutboundMessage(payload, target);
    }

    public String getTarget() {
        return target;
    }

    /**
     * Returns a new outbound message with the given payload
     */
    public OutboundMessage setTarget(String target) {
        return new OutboundMessage(payload, target);
    }

    @Override
    public String toString() {
        return "OutboundMessage{" +
                target +
                ":\t" +
                payload;
    }
}
