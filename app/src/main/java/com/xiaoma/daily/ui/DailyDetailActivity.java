package com.xiaoma.daily.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.xiaoma.daily.R;
import com.xiaoma.daily.data.AppDatabase;
import com.xiaoma.daily.data.DailyReport;
import com.xiaoma.daily.databinding.ActivityDailyDetailBinding;
import com.xiaoma.daily.util.ReportDates;

public class DailyDetailActivity extends Activity {
    public static final String EXTRA_DATE = "reportDate";
    private ActivityDailyDetailBinding binding;
    private String date;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivityDailyDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowInsetsHelper.apply(binding.getRoot());
        date = getIntent().getStringExtra(EXTRA_DATE);
        binding.date.setText(ReportDates.format(ReportDates.parse(date), "yyyy 年 M 月 d 日，EEEE"));
        binding.back.setOnClickListener(view -> finish());
        binding.write.setOnClickListener(view -> startActivity(
                new Intent(this, EditDailyActivity.class).putExtra(EXTRA_DATE, date)));
        binding.delete.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setMessage(R.string.delete_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete_report, (dialog, which) -> AppDatabase.IO.execute(() -> {
                    try {
                        AppDatabase.get(this).reports().deleteByDate(date);
                        runOnUiThread(() -> { if (!isFinishing()) finish(); });
                    } catch (Exception exception) {
                        runOnUiThread(() -> Toast.makeText(this, R.string.delete_failed, Toast.LENGTH_SHORT).show());
                    }
                })).show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextSizeSettings.apply(binding.date, 24);
        TextSizeSettings.apply(binding.content, 18);
        AppDatabase.IO.execute(() -> {
            DailyReport report = AppDatabase.get(this).reports().findByDate(date);
            runOnUiThread(() -> {
                if (isFinishing()) return;
                binding.content.setText(report == null ? getString(R.string.empty_day) : report.content);
                binding.write.setText(report == null ? R.string.write_day : R.string.edit_report);
                binding.delete.setVisibility(report == null ? View.GONE : View.VISIBLE);
            });
        });
    }
}
