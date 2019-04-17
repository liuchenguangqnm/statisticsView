package com.sunshine.appdemo.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Random;

/**
 * 日期 ---------- 维护人 ------------ 变更内容 --------
 * 2016/9/22       Sunny          请填写变更内容
 */
public class MyDatabase extends SQLiteOpenHelper {
    public MyDatabase(Context context) {
        super(context, "kilometer", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table june_2016(date VARCHAR(3), xiaoji VARCHAR(4), today BIGINT(300), total BIGINT(4))");
        Random random = new Random(5000);
        int totalKilometer = 0;
        for (int i = 1; i < 31; i ++) {
            int todayKilometer = random.nextInt(220);
            totalKilometer += todayKilometer;
            db.execSQL("Insert into june_2016(date,xiaoji,today,total) values (" + i + ",'13'," + todayKilometer + "," + totalKilometer +")");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
