package com.xiaoma.daily.ui;

import android.app.Activity;
import android.os.Bundle;

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
        binding.back.setOnClickListener(view -> finish());
    }
}
