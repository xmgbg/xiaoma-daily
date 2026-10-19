package com.xiaoma.daily.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

import com.xiaoma.daily.R;
import com.xiaoma.daily.data.AppDatabase;
import com.xiaoma.daily.data.DailyReport;
import com.xiaoma.daily.databinding.ActivityEditDailyBinding;
import com.xiaoma.daily.util.ReportDates;

public class EditDailyActivity extends Activity {
    private ActivityEditDailyBinding binding;
    private String originalContent = "";
    private boolean saving;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivityEditDailyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowInsetsHelper.apply(binding.getRoot());
        String date = getIntent().getStringExtra(DailyDetailActivity.EXTRA_DATE);
        binding.date.setText(ReportDates.format(ReportDates.parse(date), "yyyy 年 M 月 d 日，EEEE"));
        TextSizeSettings.apply(binding.date, 18);
        TextSizeSettings.apply(binding.content, 18);
        binding.back.setOnClickListener(view -> requestLeave());
        binding.save.setEnabled(false);
        AppDatabase.IO.execute(() -> {
            DailyReport report = AppDatabase.get(this).reports().findByDate(date);
            runOnUiThread(() -> {
                if (isFinishing()) return;
                originalContent = report == null ? "" : report.content;
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
            saving = true;
            binding.save.setEnabled(false);
            binding.back.setEnabled(false);
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
                        saving = false;
                        binding.save.setEnabled(true);
                        binding.back.setEnabled(true);
                        Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        requestLeave();
    }

    private void requestLeave() {
        if (saving) return;
        if (hasUnsavedChanges(originalContent, binding.content.getText().toString())) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.unsaved_changes)
                    .setNegativeButton(R.string.continue_editing, null)
                    .setPositiveButton(R.string.discard_changes, (dialog, which) -> finish())
                    .show();
        } else {
            finish();
        }
    }

    static boolean hasUnsavedChanges(String original, String current) {
        return !original.equals(current);
    }
}
