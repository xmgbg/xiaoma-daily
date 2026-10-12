package com.xiaoma.daily.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DailyReportDao {
    @Query("SELECT * FROM daily_reports WHERE reportDate = :date LIMIT 1")
    DailyReport findByDate(String date);

    @Query("SELECT reportDate FROM daily_reports WHERE reportDate >= :start AND reportDate < :end")
    List<String> datesInMonth(String start, String end);

    @Insert
    void insert(DailyReport report);

    @Update
    void update(DailyReport report);

    @Query("DELETE FROM daily_reports WHERE reportDate = :date")
    void deleteByDate(String date);
}
