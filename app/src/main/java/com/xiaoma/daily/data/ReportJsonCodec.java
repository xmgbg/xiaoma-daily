package com.xiaoma.daily.data;

import com.xiaoma.daily.util.ReportDates;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ReportJsonCodec {
    private ReportJsonCodec() { }

    public static String encode(List<DailyReport> reports) throws JSONException {
        JSONArray entries = new JSONArray();
        for (DailyReport report : reports) {
            JSONObject entry = new JSONObject();
            entry.put("reportDate", report.reportDate);
            entry.put("content", report.content);
            entry.put("createdAt", report.createdAt);
            entry.put("updatedAt", report.updatedAt);
            entries.put(entry);
        }
        JSONObject backup = new JSONObject();
        backup.put("format", "xiaoma-daily");
        backup.put("version", 1);
        backup.put("reports", entries);
        return backup.toString(2);
    }

    public static List<DailyReport> decode(String json) throws JSONException {
        JSONObject backup = new JSONObject(json);
        Object version = backup.opt("version");
        if (!"xiaoma-daily".equals(backup.opt("format")) ||
                !(version instanceof Number) || ((Number) version).doubleValue() != 1 ||
                !version.toString().equals("1")) {
            throw new JSONException("不是小马日报 v1 备份文件");
        }
        JSONArray entries = backup.getJSONArray("reports");
        List<DailyReport> reports = new ArrayList<>();
        Set<String> dates = new HashSet<>();
        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i);
            Object rawDate = entry.opt("reportDate");
            Object rawContent = entry.opt("content");
            Object rawCreated = entry.opt("createdAt");
            Object rawUpdated = entry.opt("updatedAt");
            if (!(rawDate instanceof String) || !(rawContent instanceof String) ||
                    !(rawCreated instanceof Number) || !(rawUpdated instanceof Number)) {
                throw new JSONException("第 " + (i + 1) + " 篇日报字段无效");
            }
            String date = (String) rawDate;
            try {
                ReportDates.parse(date);
            } catch (IllegalArgumentException exception) {
                throw new JSONException("第 " + (i + 1) + " 篇日报日期无效");
            }
            long created = ((Number) rawCreated).longValue();
            long updated = ((Number) rawUpdated).longValue();
            if (((Number) rawCreated).doubleValue() != created ||
                    ((Number) rawUpdated).doubleValue() != updated ||
                    ((String) rawContent).trim().isEmpty() || created <= 0 || updated < created ||
                    !dates.add(date)) {
                throw new JSONException("第 " + (i + 1) + " 篇日报内容或时间无效，或日期重复");
            }
            DailyReport report = new DailyReport();
            report.reportDate = date;
            report.content = (String) rawContent;
            report.createdAt = created;
            report.updatedAt = updated;
            reports.add(report);
        }
        return reports;
    }
}
