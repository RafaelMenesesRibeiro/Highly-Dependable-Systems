package hds.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class DateUtils {
    private static final int TOLERANCE = 5;
    private static final int FUTURE_TOLERANCE = 10;

    /**
     * Creates as a new timestamp representing a Java EPOCH starting on 1970-01-01T00:00:00Z.
     * @return long timestamp
     */
    public static long generateTimestamp() {
        Instant instant = Instant.now();
        return instant.getEpochSecond();
    }

    /**
     * @param   sentTimestamp   Long timestamp retrieved from received message
     * @return  boolean         Acknowledging or denying freshness of a message
     */
    public static boolean isFreshTimestamp(long sentTimestamp) {
        Instant instantNow = Instant.now();
        Instant sentInstant = Instant.ofEpochSecond(sentTimestamp);
        // if instantNow-Tolerance < rcvTimestamp < instantNow, then it's fresh, else it's old and should be discarded
        return sentInstant.isAfter(instantNow.minus(TOLERANCE, ChronoUnit.MINUTES));
    }

    /**
     * @param   sentTimestamp   Long timestamp retrieved from received message
     * @return  boolean         Acknowledging or denying if the sent timestamp is in the future (given a tolerance)
     */
    public static boolean isFutureTimestamp(long sentTimestamp) {
        Instant instantNow = Instant.now();
        Instant sentInstant = Instant.ofEpochSecond(sentTimestamp);
        return sentInstant.isAfter(instantNow.plus(FUTURE_TOLERANCE, ChronoUnit.SECONDS));
    }

    /**
     * @param   one             Long timestamp
     * @param   another         Long timestamp
     * @return  boolean         Acknowledging if one timestamp is is after another
     */
    public static boolean isOneTimestampAfterAnother(long one, long another) {
        return isOneInstantAfterAnother(Instant.ofEpochSecond(one), Instant.ofEpochSecond(another));
    }

    /**
     * @param   one             Instant
     * @param   another         Instant
     * @return  boolean         Acknowledging if one instant is after another
     */
    public static boolean isOneInstantAfterAnother(Instant one, Instant another) {
        return one.isAfter(another);
    }


}
