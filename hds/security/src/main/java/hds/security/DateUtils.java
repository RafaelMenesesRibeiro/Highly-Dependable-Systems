package hds.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@SuppressWarnings("Duplicates")
public class DateUtils {
    private static final int INNER_PAST_TOLERANCE = 160;
    private static final int PAST_TOLERANCE = 30;
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
     * @param   receivedTimestamp   Long timestamp retrieved from received message
     * @return  boolean         Acknowledging or denying freshness of a message
     */
    public static boolean isFreshTimestamp(long receivedTimestamp) {
        Instant instantNow = Instant.now();
        Instant receivedInstant = Instant.ofEpochSecond(receivedTimestamp);
        boolean isNotOld = receivedInstant.isAfter(instantNow.minus(PAST_TOLERANCE, ChronoUnit.SECONDS));
        boolean isNotFuture = receivedInstant.isBefore(instantNow.plus(FUTURE_TOLERANCE, ChronoUnit.SECONDS));
        return isNotOld && isNotFuture;
    }

    public static boolean isFreshTimestamp(long receivedTimestamp, int maxOldness, int maxFutureness) {
        Instant instantNow = Instant.now();
        Instant receivedInstant = Instant.ofEpochSecond(receivedTimestamp);
        boolean isNotOld = receivedInstant.isAfter(instantNow.minus(maxOldness, ChronoUnit.SECONDS));
        boolean isNotFuture = receivedInstant.isBefore(instantNow.plus(maxFutureness, ChronoUnit.SECONDS));
        return isNotOld && isNotFuture;
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

    public static int getInnerPastTolerance() {
        return INNER_PAST_TOLERANCE;
    }

    public static int getPastTolerance() {
        return PAST_TOLERANCE;
    }

    public static int getFutureTolerance() {
        return FUTURE_TOLERANCE;
    }
}
