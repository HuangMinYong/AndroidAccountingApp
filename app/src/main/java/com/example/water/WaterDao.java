package com.example.water;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface WaterDao {

    /**
     * 插入一条饮水记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(WaterRecord record);

    /**
     * 查询指定日期的总饮水量（毫升）
     *
     * @param dayStart 当天起始时间戳（00:00:00）
     * @param dayEnd   当天结束时间戳（23:59:59）
     * @return 当日总水量
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM water_record WHERE date >= :dayStart AND date <= :dayEnd")
    int getDailyTotal(long dayStart, long dayEnd);
}
