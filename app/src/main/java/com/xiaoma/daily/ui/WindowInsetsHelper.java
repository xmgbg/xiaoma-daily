package com.xiaoma.daily.ui;

import android.view.View;

final class WindowInsetsHelper {
    private WindowInsetsHelper() { }

    @SuppressWarnings("deprecation")
    static void apply(View root) {
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
        root.requestApplyInsets();
    }
}
