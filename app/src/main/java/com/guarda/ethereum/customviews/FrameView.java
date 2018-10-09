package com.guarda.ethereum.customviews;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.guarda.ethereum.R;

public class FrameView extends RelativeLayout {

    private int frameThickness = 1;
    private float frameSize = 1f;
    private int frameColor = Color.WHITE;

    private Paint paint;
    private float offset;

    public FrameView(Context context) {
        super(context);

        init(null);
    }

    public FrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public FrameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FrameView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FrameView);

            frameColor = typedArray.getColor(R.styleable.FrameView_frame_color, Color.WHITE);
            frameSize = typedArray.getFraction(R.styleable.FrameView_frame_size, 1, 1, 1f);
            if (frameSize > 0.5) {
                frameSize = 1f;
            }
            frameThickness = typedArray.getDimensionPixelSize(R.styleable.FrameView_frame_thickness, 1);

            typedArray.recycle();
        }

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(frameThickness);
        paint.setColor(frameColor);

        offset = frameThickness / 2f;
    }

    private void updatePaint() {
        paint.setStrokeWidth(frameThickness);
        paint.setColor(frameColor);

        offset = frameThickness / 2f;
    }

    public void setFrameThickness(int frameThickness) {
        this.frameThickness = frameThickness;
        updatePaint();
        invalidate();
    }

    public void setFrameColor(@ColorRes int frameColor) {
        this.frameColor = getContext().getResources().getColor(frameColor);
        updatePaint();
        invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (frameSize == 1f) {
            paint.setStrokeWidth(frameThickness * 2);

            Rect frame = new Rect();
            getDrawingRect(frame);
            canvas.drawRect(frame, paint);

        } else {
            paint.setStrokeWidth(frameThickness);

            // Left, Top
            canvas.drawLine(0, offset, width * frameSize, offset, paint);
            canvas.drawLine(offset, frameThickness, offset, height * frameSize, paint);

            // Right, Top
            canvas.drawLine(width - width * frameSize, offset, width, offset, paint);
            canvas.drawLine(width - offset, frameThickness, width - offset, height * frameSize, paint);

            // Right, Bottom
            canvas.drawLine(width - width * frameSize, height - offset, width, height - offset, paint);
            canvas.drawLine(height - offset, height - height * frameSize, width - offset, height - (frameThickness), paint);

            // Left, Bottom
            canvas.drawLine(0, height - offset, width * frameSize, height - offset, paint);
            canvas.drawLine(offset, height - height * frameSize, offset, height - (frameThickness), paint);
        }
    }
}
