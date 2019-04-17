package com.sunshine.appdemo;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sunshine.appdemo.database.MyDatabase;
import com.sunshine.appdemo.view.StatisticsViewGroup;

public class MainActivity extends AppCompatActivity {

    private StatisticsViewGroup ll_dates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {

        // 找到控件
        ll_dates = (StatisticsViewGroup) findViewById(R.id.ll_dates);

        // 获取需要展示的数据库
        MyDatabase database = new MyDatabase(getApplicationContext());
        SQLiteDatabase db = database.getReadableDatabase();

        // 设置已经存储好的数据库对象，展示数据
        ll_dates.setSaveDataBase(db);
    }

}
