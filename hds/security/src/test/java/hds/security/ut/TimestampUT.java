package hds.security.ut;

import hds.security.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TimestampUT {

    private static long now;

    @Before
    public void setNow() {
        now = DateUtils.generateTimestamp();
    }

    @Test
    public void successFreshTimestamp() {
        Assert.assertTrue(DateUtils.isFreshTimestamp(now));
    }

    @Test
    public void falseFutureTimestamp() {
        Assert.assertFalse(DateUtils.isFutureTimestamp(now));
    }

    @Test
    public void successNewerTimestamp() {
        long futureNow = now + 1;
        Assert.assertTrue(DateUtils.isOneTimestampAfterAnother(futureNow, now));
    }

    @Test
    public void failInFutureFreshTimestamp() {
        long futureNow = now + 100;
        Assert.assertFalse(DateUtils.isFreshTimestamp(futureNow));
    }

    @Test
    public void failInPastFreshTimestamp() {
        long pastNow = now - 100;
        Assert.assertFalse(DateUtils.isFreshTimestamp(pastNow));
    }

    @Test
    public void successFutureTimestamp() {
        long futureNow = now + 100;
        Assert.assertTrue(DateUtils.isFutureTimestamp(futureNow));
    }

    @Test
    public void failNewerTimestamp() {
        long futureNow = now + 1;
        Assert.assertFalse(DateUtils.isOneTimestampAfterAnother(now, futureNow));
    }

    @Test
    public void failEqualTimesNewerTimestamp() {
        Assert.assertFalse(DateUtils.isOneTimestampAfterAnother(now, now));
    }
}
