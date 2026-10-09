package com.xiaoma.daily.util;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import org.junit.Test;

public class ReportDatesTest {
    @Test
    public void monthStartsOnThursdayInOctober2026() {
        assertEquals(3, ReportDates.firstDayOffset("2026-10-05"));
    }

    @Test
    public void switchingFromMonthEndResetsDayBeforeMoving() {
        assertEquals("2026-02-01", ReportDates.shiftMonth("2026-01-31", 1));
    }

    @Test
    public void switchingMonthsCrossesYearBoundary() {
        assertEquals("2027-01-01", ReportDates.shiftMonth("2026-12-15", 1));
        assertEquals("2025-12-01", ReportDates.shiftMonth("2026-01-15", -1));
    }

    @Test
    public void leapFebruaryHas29Days() {
        assertEquals(29, ReportDates.parse("2028-02-01").getActualMaximum(Calendar.DAY_OF_MONTH));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonexistentDate() {
        ReportDates.parse("2026-02-29");
    }
}
