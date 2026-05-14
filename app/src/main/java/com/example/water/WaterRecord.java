package com.example.water;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "water_record")
public class WaterRecord {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "amount")
    private int amount;

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "date")
    private long date;

    public WaterRecord(int amount, String category, long date) {
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    // --- Getters ---

    public long getId() {
        return id;
    }

    public int getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public long getDate() {
        return date;
    }

    // --- Setters ---

    public void setId(long id) {
        this.id = id;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
