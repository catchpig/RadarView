package zhuazhu.radar.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SweepGradient;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * @author zhuazhu
 **/
public class RadarView extends View {
    /**
     * 线的宽度
     */
    private float mLineWidth;

    /**
     * 圆圈的宽度
     */
    private float mCircleWith;
    /**
     * 圆圈的个数
     */
    private int mCircleCount;
    private int mAngle  = 0;
    /**
     * 线的颜色
     */
    private int mLineColor;
    /**
     * 圆圈颜色
     */
    private int mCircleColor;
    /**
     * 扇形的颜色
     */
    private int mSweepColor;
    /**
     * 扫描的速度
     */
    private int mSweepSpeed;
    private Paint mPaintLine;
    private Paint mPaintCircle;
    private Paint mPaintSweep;

    private ValueAnimator mValueAnimator;
    public RadarView(Context context) {
        this(context,null);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.RadarView,defStyleAttr,0);
        mLineColor = typedArray.getColor(R.styleable.RadarView_line_color,Color.LTGRAY);
        mCircleColor = typedArray.getColor(R.styleable.RadarView_circle_color,Color.LTGRAY);
        mSweepColor = typedArray.getColor(R.styleable.RadarView_sweep_color,Color.BLUE);
        mCircleCount = typedArray.getInteger(R.styleable.RadarView_circle_count,3);
        mLineWidth = typedArray.getDimension(R.styleable.RadarView_line_width,5f);
        mCircleWith = typedArray.getDimension(R.styleable.RadarView_circle_width,5f);
        mSweepSpeed = typedArray.getInteger(R.styleable.RadarView_sweep_speed,2000);
        typedArray.recycle();
        initPaint();
    }
    private void initPaint(){
        //线画板
        mPaintLine = new Paint();
        mPaintLine.setAntiAlias(true);
        mPaintLine.setColor(mLineColor);
        mPaintLine.setStrokeWidth(mLineWidth);
        //圆圈画板
        mPaintCircle = new Paint();
        mPaintCircle.setAntiAlias(true);
        mPaintCircle.setColor(mCircleColor);
        mPaintCircle.setStrokeWidth(mCircleWith);
        mPaintCircle.setStyle(Paint.Style.STROKE);
        mPaintCircle.setStrokeCap(Paint.Cap.ROUND);
        //扇形画板
        mPaintSweep = new Paint();
        mPaintSweep.setAntiAlias(true);
        mPaintSweep.setStyle(Paint.Style.FILL);
        int[] colors = new int[]{Color.TRANSPARENT, changeSweepAlpha(0), changeSweepAlpha(150), changeSweepAlpha(255), changeSweepAlpha(255)};
        float[] positions = new float[]{0.0f,0.6f,0.8f,0.997f,1.0f};
        SweepGradient sweepGradient = new SweepGradient(0,0,colors,positions);
        mPaintSweep.setShader(sweepGradient);
    }
    private int centerPointX;
    private int centerPointY;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        centerPointX = width/2;
        centerPointY = height/2;
        int saveCount = canvas.saveLayer(0,0,width,height,null,Canvas.ALL_SAVE_FLAG);
        canvas.translate(centerPointX,centerPointY);
        drawCrossLine(canvas);
        drawCircle(canvas);
        drawSweep(canvas);
        canvas.restoreToCount(saveCount);
    }

    /**
     * 画十字线
     */
    private void drawCrossLine(Canvas canvas){
        //画水平线
        canvas.drawLine(-centerPointX,0,centerPointX, 0, mPaintLine);
        //画垂直线
        canvas.drawLine(0,-centerPointY, 0,centerPointY, mPaintLine);
    }

    /**
     * 画圆圈
     * @param canvas
     */
    private void drawCircle(Canvas canvas){
        for (int i = mCircleCount; i>0; i--){
            canvas.drawCircle(0,0,(centerPointX*i/ mCircleCount)- mLineWidth /2, mPaintCircle);
        }
    }


    /**
     * 画扫描器
     * @param canvas
     */
    private void drawSweep(Canvas canvas){

        canvas.rotate(mAngle,0,0);
        canvas.drawCircle(0,0,centerPointX, mPaintSweep);
    }
    /**
     * 改变扫描区颜色的透明度
     *
     * @param alpha
     * @return
     */
    private int changeSweepAlpha(int alpha) {
        int red = Color.red(mSweepColor);
        int green = Color.green(mSweepColor);
        int blue = Color.blue(mSweepColor);
        return Color.argb(alpha, red, green, blue);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnim();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnim();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if(VISIBLE==visibility){
            startAnim();
        }else{
            stopAnim();
        }
    }

    private void startAnim() {
        if (mValueAnimator == null) {
            mValueAnimator = ValueAnimator.ofInt(0, 36);
            mValueAnimator.setDuration(mSweepSpeed);
            mValueAnimator.setRepeatMode(ValueAnimator.RESTART);
            mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mValueAnimator.setInterpolator(new LinearInterpolator());
            mValueAnimator.addUpdateListener(updateListener);
        }
        if (!mValueAnimator.isStarted()) {
            mValueAnimator.start();
        }
    }

    private void stopAnim() {
        if (mValueAnimator != null && mValueAnimator.isStarted()) {
            mValueAnimator.removeUpdateListener(updateListener);
            mValueAnimator.removeAllUpdateListeners();
            mValueAnimator.cancel();
            mValueAnimator = null;
        }
    }
    private ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            int animateValue = (int) animation.getAnimatedValue();
            mAngle = 10*animateValue-90;
            invalidate();
        }
    };
}
