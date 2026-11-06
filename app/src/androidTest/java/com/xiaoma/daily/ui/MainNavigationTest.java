package com.xiaoma.daily.ui;

import static org.junit.Assert.*;

import android.app.Activity;
import android.app.Instrumentation;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.xiaoma.daily.R;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainNavigationTest {
    @Test
    public void dateSelectsPreviewAndOnlyActionOpensReport() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Instrumentation.ActivityMonitor detail = instrumentation.addMonitor(
                DailyDetailActivity.class.getName(),
                new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null), true);
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                TextView selectedDate = activity.findViewById(R.id.selectedDate);
                String previous = selectedDate.getText().toString();
                GridLayout calendar = activity.findViewById(R.id.calendar);
                View day = null;
                for (int i = 0; i < calendar.getChildCount(); i++) {
                    View cell = calendar.getChildAt(i);
                    if (cell.isClickable() && !cell.isSelected()) {
                        day = cell;
                        break;
                    }
                }
                assertNotNull(day);
                day.performClick();
                assertNotEquals(previous, selectedDate.getText().toString());
                assertEquals(0, detail.getHits());
                activity.findViewById(R.id.selectedAction).performClick();
            });
            instrumentation.waitForIdleSync();
            assertEquals(1, detail.getHits());
        } finally {
            instrumentation.removeMonitor(detail);
        }
    }

    @Test
    public void switchingTabsReusesPagesAndKeepsDate() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                View calendar = activity.findViewById(R.id.calendarPage);
                String date = ((TextView) activity.findViewById(R.id.selectedDate)).getText().toString();
                calendar.findViewById(R.id.navMine).performClick();
                View mine = activity.findViewById(R.id.minePage);
                assertEquals(View.GONE, calendar.getVisibility());
                assertEquals(View.VISIBLE, mine.getVisibility());
                mine.findViewById(R.id.navCalendar).performClick();
                assertEquals(View.VISIBLE, calendar.getVisibility());
                assertEquals(date, ((TextView) activity.findViewById(R.id.selectedDate)).getText().toString());
                calendar.findViewById(R.id.navMine).performClick();
                assertSame(mine, activity.findViewById(R.id.minePage));
                activity.onBackPressed();
                assertEquals(View.VISIBLE, calendar.getVisibility());
            });
        }
    }

    @Test
    public void recreationRestoresMineTab() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> activity.findViewById(R.id.navMine).performClick());
            scenario.recreate();
            scenario.onActivity(activity -> {
                assertEquals(View.VISIBLE, activity.findViewById(R.id.minePage).getVisibility());
                assertEquals(View.GONE, activity.findViewById(R.id.calendarPage).getVisibility());
            });
        }
    }
}
