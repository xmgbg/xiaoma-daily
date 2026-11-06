package com.xiaoma.daily.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import com.xiaoma.daily.R;
import com.xiaoma.daily.data.AppDatabase;
import com.xiaoma.daily.data.DailyReport;
import com.xiaoma.daily.databinding.ActivityMainBinding;
import com.xiaoma.daily.util.ReportDates;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity {
    private ActivityMainBinding binding;
    private String selectedDate;
    private String selectedPreview;
    private Set<String> recordedDates = new HashSet<>();

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowInsetsHelper.apply(binding.getRoot());
        selectedDate = state == null ? ReportDates.today() : state.getString("selectedDate");
        binding.previous.setOnClickListener(view -> changeMonth(-1));
        binding.next.setOnClickListener(view -> changeMonth(1));
        binding.month.setOnClickListener(view -> {
            selectedDate = ReportDates.today();
            selectedPreview = null;
            recordedDates.clear();
            renderMonth();
            loadMonth();
        });
        binding.today.setOnClickListener(view -> openDay(ReportDates.today()));
        binding.monthReset.setOnClickListener(view -> binding.month.performClick());
        binding.selectedAction.setOnClickListener(view -> openDay(selectedDate));
        binding.navCalendar.setContentDescription(getString(R.string.current_calendar));
        binding.navCalendar.setSelected(true);
        binding.navMine.setOnClickListener(view -> startActivity(new Intent(this, MineActivity.class)));
        binding.textSize.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle(R.string.text_size_title)
                .setSingleChoiceItems(R.array.text_size_levels, TextSizeSettings.level(this), (dialog, choice) -> {
                    TextSizeSettings.setLevel(this, choice);
                    applyTextSize();
                    renderMonth();
                    dialog.dismiss();
                }).show());
        renderMonth();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyTextSize();
        loadMonth();
    }

    private void applyTextSize() {
        TextSizeSettings.apply(binding.month, 18);
        TextSizeSettings.apply(binding.reportCount, 13);
        TextSizeSettings.apply(binding.selectedDate, 18);
        TextSizeSettings.apply(binding.selectedSummary, 16);
        TextSizeSettings.apply(binding.selectedAction, 14);
    }

    private void changeMonth(int offset) {
        selectedDate = ReportDates.shiftMonth(selectedDate, offset);
        selectedPreview = null;
        recordedDates.clear();
        renderMonth();
        loadMonth();
    }

    private void loadMonth() {
        String date = selectedDate;
        String start = ReportDates.monthStart(selectedDate);
        String end = ReportDates.shiftMonth(start, 1);
        AppDatabase.IO.execute(() -> {
            Set<String> dates = new HashSet<>(AppDatabase.get(this).reports().datesInMonth(start, end));
            DailyReport report = AppDatabase.get(this).reports().findByDate(date);
            runOnUiThread(() -> {
                if (!isFinishing() && date.equals(selectedDate)) {
                    recordedDates = dates;
                    selectedPreview = report == null ? null : report.content;
                    renderMonth();
                }
            });
        });
    }

    private void renderMonth() {
        Calendar month = ReportDates.parse(ReportDates.monthStart(selectedDate));
        binding.month.setText(ReportDates.format(month, "yyyy年M月"));
        binding.month.setContentDescription(getString(R.string.month_navigation, binding.month.getText()));
        binding.selectedDate.setText(ReportDates.format(ReportDates.parse(selectedDate), "M月d日  EEEE"));
        binding.reportCount.setText(getString(R.string.report_count, recordedDates.size()));
        binding.selectedSummary.setText(recordedDates.contains(selectedDate)
                ? selectedPreview == null ? getString(R.string.recorded_summary) : selectedPreview
                : getString(R.string.empty_day));
        binding.selectedAction.setText(recordedDates.contains(selectedDate) ? R.string.view_day : R.string.write_day_short);
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
        cell.setIncludeFontPadding(false);
        if (date != null) {
            SpannableString marked = new SpannableString(text + (recordedDates.contains(date) ? "\n●" : "\n "));
            marked.setSpan(new ForegroundColorSpan(getColor(date.equals(selectedDate) ? R.color.on_accent : R.color.accent)), text.length() + 1,
                    marked.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            marked.setSpan(new RelativeSizeSpan(0.35f), text.length() + 1, marked.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            cell.setText(marked);
        } else {
            cell.setText(text);
        }
        TextSizeSettings.apply(cell, date == null ? 13 : 15);
        cell.setGravity(Gravity.CENTER);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = Math.round((date == null && !text.isEmpty() ? 32 : 48) * getResources().getDisplayMetrics().density);
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        cell.setLayoutParams(params);
        if (date != null) {
            boolean selected = date.equals(selectedDate);
            cell.setBackgroundResource(R.drawable.calendar_day);
            cell.setSelected(selected);
            cell.setActivated(date.equals(ReportDates.today()));
            cell.setTextColor(getColor(selected ? R.color.on_accent : R.color.text_primary));
            cell.setFocusable(true);
            cell.setContentDescription(getString(recordedDates.contains(date) ? R.string.recorded_day_description :
                            selected ? R.string.selected_day_description : R.string.day_description,
                    ReportDates.format(ReportDates.parse(date), "yyyy 年 M 月 d 日，EEEE")));
            cell.setOnClickListener(view -> {
                selectedDate = date;
                selectedPreview = null;
                renderMonth();
                loadMonth();
            });
        } else if (text.isEmpty()) {
            cell.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        } else {
            cell.setTextColor(getColor(R.color.text_secondary));
        }
        binding.calendar.addView(cell);
    }

    private void openDay(String date) {
        selectedDate = date;
        selectedPreview = null;
        renderMonth();
        startActivity(new Intent(this, DailyDetailActivity.class).putExtra(DailyDetailActivity.EXTRA_DATE, date));
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        state.putString("selectedDate", selectedDate);
        super.onSaveInstanceState(state);
    }
}
