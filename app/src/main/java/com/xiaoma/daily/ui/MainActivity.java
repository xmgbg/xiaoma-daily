package com.xiaoma.daily.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import com.xiaoma.daily.R;
import com.xiaoma.daily.databinding.ActivityMainBinding;
import com.xiaoma.daily.util.ReportDates;

import java.util.Calendar;

public class MainActivity extends Activity {
    private ActivityMainBinding binding;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowInsetsHelper.apply(binding.getRoot());
        selectedDate = state == null ? ReportDates.today() : state.getString("selectedDate");
        binding.previous.setOnClickListener(view -> changeMonth(-1));
        binding.next.setOnClickListener(view -> changeMonth(1));
        binding.today.setOnClickListener(view -> openDay(ReportDates.today()));
        renderMonth();
    }

    private void changeMonth(int offset) {
        selectedDate = ReportDates.shiftMonth(selectedDate, offset);
        renderMonth();
    }

    private void renderMonth() {
        Calendar month = ReportDates.parse(ReportDates.monthStart(selectedDate));
        binding.month.setText(ReportDates.format(month, "yyyy 年 M 月"));
        binding.selectedDate.setText(ReportDates.format(ReportDates.parse(selectedDate), "yyyy 年 M 月 d 日，EEEE"));
        binding.calendar.removeAllViews();
        for (String weekday : getResources().getStringArray(R.array.weekdays)) {
            addCell(weekday, null);
        }
        for (int i = 0; i < ReportDates.firstDayOffset(selectedDate); i++) {
            addCell("", null);
        }
        int days = month.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int day = 1; day <= days; day++) {
            month.set(Calendar.DAY_OF_MONTH, day);
            addCell(String.valueOf(day), ReportDates.format(month, "yyyy-MM-dd"));
        }
    }

    private void addCell(String text, String date) {
        TextView cell = new TextView(this);
        cell.setText(text);
        cell.setTextSize(16);
        cell.setGravity(Gravity.CENTER);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = Math.round(56 * getResources().getDisplayMetrics().density);
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        cell.setLayoutParams(params);
        if (date != null) {
            boolean selected = date.equals(selectedDate);
            cell.setBackgroundResource(R.drawable.calendar_day);
            cell.setSelected(selected);
            cell.setActivated(date.equals(ReportDates.today()));
            cell.setTextColor(getColor(selected ? R.color.on_accent : R.color.text_primary));
            cell.setFocusable(true);
            cell.setContentDescription(getString(selected ? R.string.selected_day_description : R.string.day_description,
                    ReportDates.format(ReportDates.parse(date), "yyyy 年 M 月 d 日，EEEE")));
            cell.setOnClickListener(view -> openDay(date));
        } else if (text.isEmpty()) {
            cell.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
        binding.calendar.addView(cell);
    }

    private void openDay(String date) {
        selectedDate = date;
        renderMonth();
        startActivity(new Intent(this, DailyDetailActivity.class).putExtra(DailyDetailActivity.EXTRA_DATE, date));
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        state.putString("selectedDate", selectedDate);
        super.onSaveInstanceState(state);
    }
}
