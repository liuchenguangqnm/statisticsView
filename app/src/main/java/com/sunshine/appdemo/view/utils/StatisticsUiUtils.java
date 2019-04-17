package com.sunshine.appdemo.view.utils;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;

import java.util.ArrayList;

/**
 * 日期 ---------- 维护人 ------------ 变更内容 --------
 * 2016/9/22       Sunny          请填写变更内容
 */
public class StatisticsUiUtils {
    public static int dp2px(float dpValue, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + .5);
    }

    /**
     * 获取屏幕的宽度
     */
    public static int getScreenWidth(Context context) {
        // WindowManager windowManager = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
        // return windowManager.getDefaultDisplay().getWidth();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }
}
