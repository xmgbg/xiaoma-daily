package com.xiaoma.daily.ui;

import android.content.Context;
import android.widget.TextView;

final class TextSizeSettings {
    private static final float[] SCALES = {0.875f, 1f, 1.125f, 1.25f};

    private TextSizeSettings() { }

    static int level(Context context) {
        int value = context.getSharedPreferences("settings", Context.MODE_PRIVATE).getInt("textSize", 1);
        return value >= 0 && value < SCALES.length ? value : 1;
    }

    static void setLevel(Context context, int level) {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putInt("textSize", level).apply();
    }

    static void apply(TextView view, float baseSp) {
        view.setTextSize(baseSp * SCALES[level(view.getContext())]);
    }
}
