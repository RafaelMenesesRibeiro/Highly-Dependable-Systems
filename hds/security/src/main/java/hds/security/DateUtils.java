package hds.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class DateUtils {
    private static final int TOLERANCE = 5;
    /**
     * Creates as a new timestamp representing a Java EPOCH starting on 1970-01-01T00:00:00Z.
     * @return long timestamp
     */
    public static long generateTimestamp() {
        Instant instant = Instant.now();
        return instant.getEpochSecond();
    }

    /**
     * @param sentTimestamp long timestamp retrieved from received message
     * @return boolean acknowledging or denying freshness of a message
     */
    public static boolean isFreshTimestamp(long sentTimestamp) {
        Instant instantNow = Instant.now();
        Instant sentInstant = Instant.ofEpochSecond(sentTimestamp);
        // if instantNow-Tolerance < rcvTimestamp < instantNow, then it's fresh, else it's old and should be discarded
        return sentInstant.isAfter(instantNow.minus(TOLERANCE, ChronoUnit.MINUTES));
    }
}
