package util;

import java.time.LocalDateTime;

/**
 * Created by Dominic Hauton on 09/03/2016.
 */
public class DateTimeDecorator {
    private LocalDateTime currentTime;

    public DateTimeDecorator() {
        currentTime = LocalDateTime.now();
    }

    public LocalDateTime getEpoch() {
        return currentTime;
    }
}
