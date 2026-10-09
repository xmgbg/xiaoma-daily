package com.xiaoma.daily.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.xiaoma.daily.databinding.ActivityDailyDetailBinding;
import com.xiaoma.daily.util.ReportDates;

public class DailyDetailActivity extends Activity {
    public static final String EXTRA_DATE = "reportDate";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        ActivityDailyDetailBinding binding = ActivityDailyDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowInsetsHelper.apply(binding.getRoot());
        String date = getIntent().getStringExtra(EXTRA_DATE);
        binding.date.setText(ReportDates.format(ReportDates.parse(date), "yyyy 年 M 月 d 日，EEEE"));
        binding.back.setOnClickListener(view -> finish());
        binding.write.setOnClickListener(view -> startActivity(
                new Intent(this, EditDailyActivity.class).putExtra(EXTRA_DATE, date)));
    }
}
