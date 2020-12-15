package xxxxx.xxx

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.qh.tesla.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 雷达图
 */
public class RadarView extends View {

    // 雷达区画笔
    private Paint mMainPaint;
    // 圆个数
    private int mCircleCount = 5;
    // 成绩圆点半径
    private int mValueRadius = 8;
    // 成绩背景透明度
    private int mValueAlphaColor = 25;
    // 数据个数
    private int mCount = 4;
    // 网格最大半径
    private float mRadius;
    // 网格最大半径系数，最大为1
    private float mRadiusMultiple = 0.6f;
    // 文本画笔
    private Paint mTextPaint;
    // 文本成绩画笔
    private Paint mTextAchievementPaint;
    // 数据区画笔
    private Paint mValuePaint;
    // 中心X
    private float mCenterX;
    // 中心Y
    private float mCenterY;
    // 标题文字
    private List<String> mTitles;
    // 各维度分值
    private List<Integer> mData;
    // 数据最大值
    private int mMaxValue = 100;
    // 线终点坐标
    private float[][] mPoint;

    // 间距偏移量
    private int mMargin = 80;


    public RadarView(Context context) {
        this(context, null);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 雷达区画笔初始化
        mMainPaint = new Paint();
        mMainPaint.setColor(getResources().getColor(R.color.radar_circle_line));
        mMainPaint.setAntiAlias(true);
        mMainPaint.setStrokeWidth(1);
        mMainPaint.setStyle(Paint.Style.STROKE);
        //文本画笔初始化
        mTextPaint = new Paint();
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(getResources().getDimension(R.dimen.radar_font_size));
        mTextPaint.setColor(getResources().getColor(R.color.radar_title_text));
        mTextPaint.setStrokeWidth(1);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/FZY3JW.TTF"));
        //文本成绩画笔初始化
        mTextAchievementPaint = new Paint();
        mTextAchievementPaint.setTextAlign(Paint.Align.CENTER);
        mTextAchievementPaint.setTextSize(getResources().getDimension(R.dimen.radar_font_size));
        mTextAchievementPaint.setColor(getResources().getColor(R.color.radar_data_text));
        mTextAchievementPaint.setStrokeWidth(1);
        mTextAchievementPaint.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/FZY3JW.TTF"));
        mTextAchievementPaint.setAntiAlias(true);
        //数据区（分数）画笔初始化
        mValuePaint = new Paint();
        //mValuePaint.setColor(getResources().getColor(R.color.radar_data_color));
        mValuePaint.setAntiAlias(true);
        mValuePaint.setStrokeWidth(5);
        LinearGradient lg = new LinearGradient(0, 0, 5, 5,
                getResources().getColor(R.color.radar_data_start_color),
                getResources().getColor(R.color.radar_data_end_color), Shader.TileMode.MIRROR);
        mValuePaint.setShader(lg);
        mValuePaint.setStyle(Paint.Style.FILL);

        mTitles = new ArrayList<>();
        mTitles.add("数");
        mTitles.add("图形");
        mTitles.add("语言");
        mTitles.add("逻辑思考");
        mCount = mTitles.size();

        //默认分数
        mData = new ArrayList<>(mCount);
        mData.add(100);
        mData.add(100);
        mData.add(80);
        mData.add(60);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mRadius = Math.min(w, h) / 2 * mRadiusMultiple;
        mCenterX = w / 2;
        mCenterY = h / 2;
        //一旦size发生改变，重新绘制
        postInvalidate();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 根据数据项目数，计算每条线坐标
        setPoint();
        // 绘制蜘蛛网
        drawPolygon(canvas);
        // 绘制直线
        drawLines(canvas);
        // 绘制标题
        drawTitle(canvas);
        // 绘制覆盖区域
        drawRegion(canvas);
    }

    private void setPoint() {
        //mCount = 2;
        mPoint = new float[mCount][2];
        // 每个扇形区域所占角度
        int avgAngle = 360 / mCount;
        for (int i = 0; i < mCount; i++) {
            // 根据间隔角度计算出每个菜单相对于水平线起始位置的真实角度
            int angle = avgAngle * (i + 1) - 90;
            float x = (float)Math.cos(Math.toRadians(angle)) * mRadius;
            float y = (float)Math.sin(Math.toRadians(angle)) * mRadius;
            mPoint[i][0] = x;
            mPoint[i][1] = y;
        }
    }

    /**
     * 绘制多边形
     *
     * @param canvas
     */
    private void drawPolygon(Canvas canvas) {
        // 每个圆之间的间距
        float r = mRadius / mCircleCount;
        // 当前半径
        float curR = r;
        for (int i = 1; i <= mCircleCount; i++) {
            canvas.drawCircle(mCenterX, mCenterY, r * i, mMainPaint);
        }
    }


    /**
     * 绘制直线
     */
    private void drawLines(Canvas canvas) {
        for (int i = 0; i < mCount; i++) {
            canvas.drawLine(mCenterX, mCenterY,
                    mCenterX + mPoint[i][0], mCenterY + mPoint[i][1], mMainPaint);
        }
    }

    /**
     * 绘制标题文字
     *
     * @param canvas
     */
    private void drawTitle(Canvas canvas) {
        if (mCount != mTitles.size()) {
            return;
        }
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        // 获取文字高度
        float fontHeight = fontMetrics.descent - fontMetrics.ascent;
        //绘制文字
        for (int i = 0; i < mCount; i++) {
            // 文字默认区域
            float x = mCenterX + mPoint[i][0];
            float y = mCenterY + mPoint[i][1];
            // 防止文字与雷达图重合，如果文字在雷达图右侧，文字位置右移，否则左移
            if (x > mCenterX) {
                x = x + mMargin;
            } else if (x < mCenterX) {
                x = x - mMargin;
            }
            // 防止文字与雷达图重合，如果文字在雷达图上方，文字位置上移，否则下移
            if (y > mCenterY) {
                y = y + mMargin;
            } else if (y < mCenterY) {
                y = y - mMargin;
            }
            // 类目
            canvas.drawText(mTitles.get(i), x, y, mTextPaint);
            // 百分比
            canvas.drawText(mData.get(i) + "%", x, y + fontHeight, mTextAchievementPaint);
        }
    }

    /**
     * 绘制覆盖区域
     */
    private void drawRegion(Canvas canvas) {
        Path path = new Path();
        float dataValue;
        float percent;
        mValuePaint.setAlpha(255);
        for (int i = 0; i < mCount; i++) {
            // 根据当前成绩画圆点
            dataValue = mData.get(i);
            if (dataValue != mMaxValue) {
                percent = dataValue / mMaxValue;
            } else {
                percent = 1;
            }
            float x = mCenterX + mPoint[i][0] * percent;
            float y = mCenterY + mPoint[i][1] * percent;
            if (i == 0) {
                // 开始位置
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
            canvas.drawCircle(x, y, mValueRadius, mValuePaint);
        }
        path.close();
        mValuePaint.setStyle(Paint.Style.STROKE);
        //绘制覆盖区域外的连线
        canvas.drawPath(path, mValuePaint);
        //填充覆盖区域
        mValuePaint.setAlpha(mValueAlphaColor);
        mValuePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawPath(path, mValuePaint);
    }


    //设置蜘蛛网颜色
    public void setMainPaint(Paint mainPaint) {
        this.mMainPaint = mainPaint;
        postInvalidate();
    }

    //设置标题颜色
    public void setTextPaint(Paint textPaint) {
        this.mTextPaint = textPaint;
    }

    //设置成绩颜色
    public void setTextAchievementPaint(Paint textPaint) {
        this.mTextAchievementPaint = textPaint;
    }

    //设置覆盖局域颜色
    public void setValuePaint(Paint valuePaint) {
        this.mValuePaint = valuePaint;
        postInvalidate();
    }

    //设置课程分类
    public void setTitles(List<String> titles) {
        this.mTitles = titles;
        this.mCount = titles.size();
        postInvalidate();
    }

    //设置各门得分
    public void setData(List<Integer> data) {
        this.mData = data;
        postInvalidate();
    }

    //设置满分分数，默认是100分满分
    public void setMaxValue(int maxValue) {
        this.mMaxValue = maxValue;
    }

    public void setMargin(int mMargin) {
        this.mMargin = mMargin;
    }
}
