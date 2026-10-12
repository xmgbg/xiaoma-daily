package com.xiaoma.daily.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "daily_reports", indices = @Index(value = "reportDate", unique = true))
public class DailyReport {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @NonNull
    public String reportDate = "";
    @NonNull
    public String content = "";
    public long createdAt;
    public long updatedAt;
}
