package com.xiaoma.daily.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EditDailyActivityTest {
    @Test
    public void unchangedOrRevertedContentDoesNotNeedConfirmation() {
        assertFalse(EditDailyActivity.hasUnsavedChanges("原日报", "原日报"));
        assertFalse(EditDailyActivity.hasUnsavedChanges("", ""));
    }

    @Test
    public void newOrEditedContentNeedsConfirmation() {
        assertTrue(EditDailyActivity.hasUnsavedChanges("", "新日报"));
        assertTrue(EditDailyActivity.hasUnsavedChanges("原日报", "修改后"));
        assertTrue(EditDailyActivity.hasUnsavedChanges("", " "));
    }
}
