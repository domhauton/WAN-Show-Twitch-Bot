package message;

/**
 * Created by Dominic H on 09/08/2015 at 22:59.
 *
 * Stores a penalty issued for the message.
 */
public class Penalty {
    private Integer penalty;
    private String reason;

    public Penalty(Integer penalty, String reason) {
        if(penalty == null || reason == null) throw new IllegalArgumentException();
        this.penalty = penalty;
        this.reason = reason;
    }

    public Integer getPenalty() {
        return penalty;
    }

    public String getReason() {
        return reason;
    }
}
