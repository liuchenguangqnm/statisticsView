package com.sunshine.appdemo.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sunshine.appdemo.R;
import com.sunshine.appdemo.view.utils.StatisticsUiUtils;

import java.util.ArrayList;

/**
 * 日期 ---------- 维护人 ------------ 变更内容 --------
 * 2016/9/22       Sunny          请填写变更内容
 */
public class StatisticsViewGroup extends ViewGroup {
    private ArrayList<View> views = new ArrayList<>();
    // 宽高默认值
    private int statisticsViewHeight = StatisticsUiUtils.dp2px(200, getContext());
    private int datesWidth = StatisticsUiUtils.dp2px(45, getContext());
    private int bottomTextSize = 14;
    private int scrollBarHeight = StatisticsUiUtils.dp2px(30, getContext());
    private int bottomTextInterval = 1;  // 底部文字单位显示间隔
    private int viewBackgroundColor = 0xFF11E0F2;
    private boolean isWidthMatch = false;
    // 数据条数记录
    private int dateNum = 0;
    // 用于判断当前View是否需要重新布局
    private static boolean flag;

    public StatisticsViewGroup(Context context) {
        this(context, null);
    }

    public StatisticsViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatisticsViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray params = context.obtainStyledAttributes(attrs, R.styleable.StatisticsViewAttrs);
            statisticsViewHeight = StatisticsUiUtils.dp2px(params.getDimension(R.styleable.StatisticsViewAttrs_statistics_view_height, 200), getContext());
            datesWidth = StatisticsUiUtils.dp2px(params.getDimension(R.styleable.StatisticsViewAttrs_per_date_width, 45), getContext());
            bottomTextSize = (int) params.getDimension(R.styleable.StatisticsViewAttrs_date_text_size, 14);
            scrollBarHeight = StatisticsUiUtils.dp2px(params.getDimension(R.styleable.StatisticsViewAttrs_date_text_view_height, 30), getContext());
            if (params.getBoolean(R.styleable.StatisticsViewAttrs_is_big_interval_at_bottom_text, false))
                bottomTextInterval = 5;
            else
                bottomTextInterval = 1;
            String colorString = params.getString(R.styleable.StatisticsViewAttrs_path_view_theme_color);
            if (colorString.length() < 9) {
                colorString = colorString.replace("#", "#FF");
            } else if (colorString.length() < 5) {
                colorString = colorString.replace("#", "#F");
            }
            colorString = colorString.toUpperCase();
            viewBackgroundColor = Color.parseColor(colorString);
            isWidthMatch = params.getBoolean(R.styleable.StatisticsViewAttrs_is_width_match, false);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int halfTextUnit = (int) (2.5 * datesWidth + .5);
        int minTextUnit = (int) (0.26 * datesWidth + .5);
        for (int i = 0; i < views.size(); i++) {
            if (i < views.size() - 1) {
                if (bottomTextInterval != 1) {
                    if (i == 0) {
                        views.get(i).layout(minTextUnit, statisticsViewHeight, halfTextUnit, scrollBarHeight + statisticsViewHeight);
                    } else if (i == views.size() - 2) {
                        if (isWidthMatch)
                            views.get(i).layout(i * datesWidth - halfTextUnit - minTextUnit,
                                    statisticsViewHeight, i * datesWidth + halfTextUnit, scrollBarHeight + statisticsViewHeight);
                        else
                            views.get(i).layout(i * datesWidth,
                                    statisticsViewHeight, (i + 1) * datesWidth, scrollBarHeight + statisticsViewHeight);
                    } else {
                        views.get(i).layout(i * datesWidth - halfTextUnit,
                                statisticsViewHeight, i * datesWidth + halfTextUnit,
                                scrollBarHeight + statisticsViewHeight);
                    }
                } else {
                    if (i == 0) {
                        views.get(i).layout(i * datesWidth + minTextUnit, statisticsViewHeight,
                                (i + 1) * datesWidth, scrollBarHeight + statisticsViewHeight);
                    } else if (i == views.size() - 2) {
                        views.get(i).layout((int) (i * datesWidth + minTextUnit * 0.5f), statisticsViewHeight,
                                (i + 1) * datesWidth, scrollBarHeight + statisticsViewHeight);
                    } else {
                        views.get(i).layout(i * datesWidth, statisticsViewHeight,
                                (i + 1) * datesWidth, scrollBarHeight + statisticsViewHeight);
                    }
                }
            } else {
                views.get(i).layout(0, 0, datesWidth * (views.size() - 1), statisticsViewHeight);
            }
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(datesWidth * (views.size() - 1), statisticsViewHeight + scrollBarHeight);
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        views.add(child);

        if (flag) {
            requestLayout();
        }

        // 恢复原有赋值
        flag = false;
    }

    /**
     * 设置已经存储好的数据库对象，展示数据
     */
    public void setSaveDataBase(SQLiteDatabase db) {
        // 查询数据条数，顺带通过倒序排列的方式获取到最大的统计数据
        Cursor queryTheLargeNum = db.rawQuery("select today from june_2016 order by today desc", null);
        dateNum = queryTheLargeNum.getCount();

        if (isWidthMatch) {
            datesWidth = (int) (StatisticsUiUtils.getScreenWidth(getContext()) * 1.0f / dateNum + .5f);
        }

        if (bottomTextInterval == 1)
            if (isWidthMatch && dateNum >= 15) {
                bottomTextInterval = 5;
            } else {
                bottomTextInterval = 1;
            }

        if (dateNum * datesWidth > StatisticsUiUtils.dp2px(45, getContext()) * dateNum)
            Toast.makeText(getContext(), "绘制宽度过长可能导致二阶贝赛尔曲线无法绘制！！", Toast.LENGTH_LONG).show();

        // 逐个添加日期
        for (int i = 1; i <= dateNum; i++) {
            TextView view = new TextView(getContext());
            view.setTextSize(bottomTextSize);
            view.setTextColor(Color.WHITE);

            if (i == 1 || i % bottomTextInterval == 0) {
                if (i == 1) {
                    view.setGravity(Gravity.LEFT);
                } else {
                    view.setGravity(Gravity.CENTER);
                }
                view.setText("6-" + i);
                view.setWidth(datesWidth);
            } else {
                view.setText("");
                view.setWidth(0);
            }

            view.setHeight(scrollBarHeight + StatisticsUiUtils.dp2px(5, getContext()));
            view.setPadding(0, StatisticsUiUtils.dp2px(5, getContext()), 0, 0);
            addView(view);
        }

        // 最后添加统计波浪图
        StatisticsView statisticsView = new StatisticsView(getContext(), dateNum, db, queryTheLargeNum, datesWidth, statisticsViewHeight, viewBackgroundColor);
        flag = true;
        addView(statisticsView);
    }
}
