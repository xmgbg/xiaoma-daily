package com.xiaoma.daily.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.xiaoma.daily.R;
import com.xiaoma.daily.data.AppDatabase;
import com.xiaoma.daily.data.DailyReport;
import com.xiaoma.daily.databinding.ActivityEditDailyBinding;
import com.xiaoma.daily.util.ReportDates;

public class EditDailyActivity extends Activity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        ActivityEditDailyBinding binding = ActivityEditDailyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowInsetsHelper.apply(binding.getRoot());
        String date = getIntent().getStringExtra(DailyDetailActivity.EXTRA_DATE);
        binding.date.setText(ReportDates.format(ReportDates.parse(date), "yyyy 年 M 月 d 日，EEEE"));
        TextSizeSettings.apply(binding.date, 18);
        TextSizeSettings.apply(binding.content, 18);
        binding.back.setOnClickListener(view -> finish());
        binding.save.setEnabled(false);
        AppDatabase.IO.execute(() -> {
            DailyReport report = AppDatabase.get(this).reports().findByDate(date);
            runOnUiThread(() -> {
                if (isFinishing()) return;
                if (report != null && state == null) binding.content.setText(report.content);
                binding.save.setEnabled(true);
            });
        });
        binding.save.setOnClickListener(view -> {
            String content = binding.content.getText().toString();
            if (content.trim().isEmpty()) {
                binding.content.setError(getString(R.string.empty_content));
                return;
            }
            binding.save.setEnabled(false);
            AppDatabase.IO.execute(() -> {
                try {
                    DailyReport report = AppDatabase.get(this).reports().findByDate(date);
                    long now = System.currentTimeMillis();
                    if (report == null) {
                        report = new DailyReport();
                        report.reportDate = date;
                        report.content = content;
                        report.createdAt = now;
                        report.updatedAt = now;
                        AppDatabase.get(this).reports().insert(report);
                    } else {
                        report.content = content;
                        report.updatedAt = now;
                        AppDatabase.get(this).reports().update(report);
                    }
                    runOnUiThread(() -> { if (!isFinishing()) finish(); });
                } catch (Exception exception) {
                    runOnUiThread(() -> {
                        binding.save.setEnabled(true);
                        Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }
}
