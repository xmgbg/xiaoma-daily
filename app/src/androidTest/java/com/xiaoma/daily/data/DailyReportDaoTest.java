package com.xiaoma.daily.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

@RunWith(AndroidJUnit4.class)
public class DailyReportDaoTest {
    @Test
    public void saveUpdateAndDeleteAppearInMonthQuery() {
        AppDatabase database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
                AppDatabase.class).allowMainThreadQueries().build();
        try {
            DailyReport report = new DailyReport();
            report.reportDate = "2026-10-12";
            report.content = "第一版";
            report.createdAt = 1;
            report.updatedAt = 1;
            database.reports().insert(report);

            assertEquals(Collections.singletonList("2026-10-12"),
                    database.reports().datesInMonth("2026-10-01", "2026-11-01"));
            DailyReport saved = database.reports().findByDate("2026-10-12");
            saved.content = "修改后";
            saved.updatedAt = 2;
            database.reports().update(saved);
            assertEquals("修改后", database.reports().findByDate("2026-10-12").content);
            assertEquals(1, database.reports().findByDate("2026-10-12").createdAt);

            database.reports().deleteByDate("2026-10-12");
            assertNull(database.reports().findByDate("2026-10-12"));
            assertEquals(0, database.reports().datesInMonth("2026-10-01", "2026-11-01").size());
        } finally {
            database.close();
        }
    }
}
