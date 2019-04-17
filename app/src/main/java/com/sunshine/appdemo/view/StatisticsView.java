package com.sunshine.appdemo.view;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.sunshine.appdemo.view.utils.StatisticsUiUtils;

import java.util.ArrayList;

/**
 * 日期 ---------- 维护人 ------------ 变更内容 --------
 * 2016/9/22       Sunny          请填写变更内容
 */
public class StatisticsView extends View {
    private final Context context;
    private Cursor queryMostLargeData;
    private int viewHeight;
    private int datesWidth;
    private int viewBackgroundColor;
    private SQLiteDatabase db;
    private int dateNum;
    private float perDayWidth;
    private float perPercentHeight;
    private ArrayList<Integer> dataSet;
    private ArrayList<Point> points;
    private Integer nextJourney;
    private int mostLargeData;

    public StatisticsView(Context context, int dateNum, SQLiteDatabase db, Cursor queryMostLargeData, int datesWidth, int viewHeight, int viewBackgroundColor) {
        this(context, null);
        this.dateNum = dateNum;
        this.db = db;
        this.queryMostLargeData = queryMostLargeData;
        this.datesWidth = datesWidth;
        this.viewHeight = viewHeight;
        this.viewBackgroundColor = viewBackgroundColor;
    }

    public StatisticsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatisticsView(final Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        // 获取统计数据中最大的记录
        queryMostLargeData.moveToFirst();
        mostLargeData = queryMostLargeData.getInt(0) + 5;

        // 平均每天数据曲线的宽度
        perDayWidth = ((float) datesWidth * dateNum) / dateNum;
        // 平均每单位数据应当占据的高度
        perPercentHeight = ((float) viewHeight) / mostLargeData;


        // 从数据库逐条查询需要绘制的数据，并新建集合存储之
        Cursor queryTotalMonth = db.query("june_2016", null, null, null, null, null, null);
        dataSet = new ArrayList<>();

        // 首先初始化每日的数据
        int currentJourney = 0;

        // 正式开始查询，将数据封装到集合中：
        while (queryTotalMonth.moveToNext()) {
            currentJourney = queryTotalMonth.getInt(queryTotalMonth.getColumnIndex("today"));
            dataSet.add(currentJourney);
        }

        // 准备好存储所有点的集合
        points = new ArrayList<>();
        for (int i = 1; i <= dateNum; i++) {
            // 获得某天的骑行路程
            currentJourney = dataSet.get(i - 1);
            // 获得下一天骑行的路程
            if (i != dateNum) {
                nextJourney = dataSet.get(i);
            }

            // 准备好将要连接的点
            preDrawPath(i, currentJourney, nextJourney);
        }

        // 绘制图形
        drawPath(canvas);
    }

    /**
     * 做绘制曲线之前的准备工作
     */
    private void preDrawPath(int date, int currentJourney, int nextJourney) {
        // 初始化二阶贝塞尔曲线需要连接的各个点，并且放置到points集合中去
        int lowerY;
        Point pointFirst = null;

        // 其次初始化贝赛尔曲线的顶点
        int tipX = (int) ((perDayWidth * (date - 1)) + perDayWidth / 2 + .5);
        int tipY = StatisticsUiUtils.dp2px(200, context) - (int) (currentJourney * perPercentHeight + .5);
        Point pointTip = new Point(tipX, tipY);

        // 最后初始化平滑连接低谷的端点
        int lowerX = (int) (date * perDayWidth + .5);
        lowerY = (int) (StatisticsUiUtils.dp2px(200, context) - (nextJourney * perPercentHeight) + StatisticsUiUtils.dp2px(20, context) + .5);
        // 如果当前顶点和下一个顶点处于上升阶段，低谷端点的Y值可能会和相邻顶点的Y值相差无几
        // 如果真的相差无几，此时就需要以当前的点为参考，首先获取俩 tipY 中点的偏移量
        // 首先需要判断当前端点对比下一个端点是上升还是下降，根据上升下降趋势来调整点的偏移量
        boolean isTipUpping = nextJourney > currentJourney ? true : false;
        if (isTipUpping) { // 如果处于上升阶段,并且俩端点中点的Y值和相邻两个顶点的Y值相差无几，低点Y坐标以当前顶点为参考
            // 初始化下一天 tipY 的坐标
            int nextTipY = (int) (StatisticsUiUtils.dp2px(200, context) - nextJourney * perPercentHeight + .5);
            // 求两者的平均值
            int averageY = (int) ((tipY + nextTipY) / 2.0 + .5);
            // 计算 lowerY 的值是否和两点Y方向的平均值十分近似，据此判断是否需要做向下修正
            int subtractLowerYAndAverageY = 0;
            if (averageY > lowerY) {
                subtractLowerYAndAverageY = averageY - lowerY;
            } else if (averageY <= lowerY) {
                subtractLowerYAndAverageY = lowerY - averageY;
            }
            if (subtractLowerYAndAverageY < StatisticsUiUtils.dp2px(15, context)) {
                lowerY += StatisticsUiUtils.dp2px(8, context);
                lowerX -= StatisticsUiUtils.dp2px(7, context); // 为了让线段显示得更平滑，修改低谷点的X偏移量
            }
        }
        // 如果低谷端点低于控件的显示区域
        if (lowerY > StatisticsUiUtils.dp2px(200, context)) {
            lowerY = StatisticsUiUtils.dp2px(200, context) - StatisticsUiUtils.dp2px(5, context);
        }
        // 如果是最后一个低谷端点，即最后一个曲线点
        if (nextJourney == 0) {
            lowerY = (int) (StatisticsUiUtils.dp2px(200, context) - (currentJourney * perPercentHeight) + StatisticsUiUtils.dp2px(20, context) + .5);
        }
        Point pointLink = new Point(lowerX, lowerY);

        // 为了连接平滑的波浪线，我们还要在顶点和低点中间多造两个点，让二阶贝塞尔曲线获得足够的控制点平滑连接
        // 首先求出上升方向的线段中点
        Point upLineCenter = new Point();
        int previousLowerPointX = 0;
        int previousLowerPointY = 0;
        if (date == 1) {
            pointFirst = new Point
                    (0, (int) (StatisticsUiUtils.dp2px(200, context) - (currentJourney * perPercentHeight + .5) + StatisticsUiUtils.dp2px(20, context)));
            upLineCenter.x = pointFirst.x + (int) ((pointTip.x - pointFirst.x) / 2.0 + .5);
            upLineCenter.y = pointFirst.y + (int) ((pointTip.y - pointFirst.y) / 2.0 + .5);
        } else {
            previousLowerPointX = (int) ((date - 1) * perDayWidth + .5);
            previousLowerPointY = (int) (pointTip.y + StatisticsUiUtils.dp2px(20, context) + .5);
            if (previousLowerPointY > StatisticsUiUtils.dp2px(200, context)) {
                previousLowerPointY = StatisticsUiUtils.dp2px(200, context) - StatisticsUiUtils.dp2px(5, context);
            }
            upLineCenter.x = previousLowerPointX + (int) ((pointTip.x - previousLowerPointX) / 2.0 + .5);
            upLineCenter.y = previousLowerPointY + (int) ((pointTip.y - previousLowerPointY) / 2.0 + .5);
        }

        // 首先需要判断当前的线段实际上是在上升还是在下降，根据上升下降趋势来调整点的偏移量
        boolean isDowning = nextJourney > currentJourney ? true : false;
        // 再来求出下降方向的线段中点
        Point downLineCenter = new Point();
        int lastPointY = pointTip.y + StatisticsUiUtils.dp2px(10, context) + StatisticsUiUtils.dp2px(10, context);
        if (isDowning) {
            if (date == dateNum) {
                downLineCenter.x = pointTip.x + (int) ((perDayWidth * dateNum - pointTip.x) / 2.0 + .5);
                downLineCenter.y = pointTip.y + (int) ((lastPointY - pointTip.y) / 2.0 + .5);
            } else {
                downLineCenter.x = pointTip.x + (int) ((lowerX - pointTip.x) / 2.0 + .5);
                downLineCenter.y = pointTip.y + (int) ((lowerY - pointTip.y) / 2.0 + .5);
            }
        } else {
            downLineCenter.x = pointTip.x + (int) ((lowerX - pointTip.x) / 2.0 + .5);
            ;
            downLineCenter.y = pointTip.y - (int) ((pointTip.y - lowerY) / 2.0 + .5);
        }

        // 最后统一按照顺序来加点，让我们的曲线按照顺序逐一绘制点
        if (date == 1) {
            points.add(pointFirst);
        }
        // 由于顶点部分是贝赛尔曲线的控制点，所以需要抬升顶点Y坐标以减少误差
        points.add(new Point(upLineCenter.x, upLineCenter.y + StatisticsUiUtils.dp2px(6, context)));
        points.add(new Point(pointTip.x, pointTip.y + StatisticsUiUtils.dp2px(10, context)));
        points.add(new Point(downLineCenter.x, downLineCenter.y + StatisticsUiUtils.dp2px(6, context)));
        if (date != dateNum) {
            points.add(pointLink);
        }
        if (date == dateNum) {
            Point pointLast = new Point((int) (dateNum * perDayWidth + .5), lastPointY);
            points.add(pointLast);
        }
    }

    public void drawPath(Canvas canvas) {
        // 初始化画笔1
        Paint paint1 = new Paint();
        paint1.setStyle(Paint.Style.FILL);
        paint1.setAntiAlias(true);

        // 绘制渐变色背景
        LinearGradient lg = new LinearGradient(0, perPercentHeight * 5, 0, viewHeight, Color.WHITE, viewBackgroundColor, Shader.TileMode.CLAMP);
        paint1.setShader(lg);
        canvas.drawRect(0, perPercentHeight * 5, points.get(points.size() - 1).x, viewHeight, paint1);

        // 初始化画笔2
        Paint paint2 = new Paint();
        paint2.setStyle(Paint.Style.FILL);
        paint2.setAntiAlias(true);
        paint2.setColor(viewBackgroundColor);
        // 初始化路径，绘制顶部原色遮盖
        Path path = new Path();
        path.moveTo(points.get(0).x, points.get(0).y);
        path.lineTo(points.get(1).x, points.get(1).y);
        for (int i = 2; i < points.size() - 1; i += 2) {
            path.quadTo(points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y);
        }
        path.lineTo(points.get(points.size() - 1).x, points.get(points.size() - 1).y);
        path.lineTo(points.get(points.size() - 1).x, 0);
        path.lineTo(0, 0);
        path.close();
        canvas.drawPath(path, paint2);

        // 最后绘制白色的线条曲线
        path.reset();
        path.moveTo(points.get(0).x, points.get(0).y);
        path.lineTo(points.get(1).x, points.get(1).y);
        for (int i = 2; i < points.size() - 1; i += 2) {
            path.quadTo(points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y);
        }
        path.lineTo(points.get(points.size() - 1).x, points.get(points.size() - 1).y);
        // 初始化画笔3
        Paint paint3 = new Paint();
        // 根据像素密度比来设置画笔粗细
        float density = context.getResources().getDisplayMetrics().density;
        if (density <= 1) {
            paint3.setStrokeWidth((float) 1.0);
        } else {
            paint3.setStrokeWidth((float) 5.0);
        }
        paint3.setAntiAlias(true);
        paint3.setColor(Color.WHITE);
        paint3.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint3);
    }
}
