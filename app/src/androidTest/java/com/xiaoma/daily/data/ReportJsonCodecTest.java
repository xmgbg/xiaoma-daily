package com.xiaoma.daily.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ReportJsonCodecTest {
    @Test
    public void exportAndImportKeepAllFields() throws JSONException {
        DailyReport report = new DailyReport();
        report.reportDate = "2026-10-13";
        report.content = "今天完成了日报。\n第二行";
        report.createdAt = 1000;
        report.updatedAt = 2000;

        List<DailyReport> decoded = ReportJsonCodec.decode(
                ReportJsonCodec.encode(Arrays.asList(report)));
        assertEquals(1, decoded.size());
        assertEquals(report.reportDate, decoded.get(0).reportDate);
        assertEquals(report.content, decoded.get(0).content);
        assertEquals(report.createdAt, decoded.get(0).createdAt);
        assertEquals(report.updatedAt, decoded.get(0).updatedAt);
    }

    @Test
    public void invalidLastEntryRejectsWholeFile() {
        String json = "{\"format\":\"xiaoma-daily\",\"version\":1,\"reports\":["
                + "{\"reportDate\":\"2026-10-12\",\"content\":\"有效\",\"createdAt\":1,\"updatedAt\":1},"
                + "{\"reportDate\":\"2026-10-12\",\"content\":\"重复\",\"createdAt\":1,\"updatedAt\":1}]}";
        try {
            ReportJsonCodec.decode(json);
            fail("Duplicate dates must reject the file");
        } catch (JSONException expected) {
            // No list is returned for a partially valid backup.
        }
    }
}
