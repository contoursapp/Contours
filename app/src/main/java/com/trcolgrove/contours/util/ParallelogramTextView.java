package com.trcolgrove.contours.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.TextView;

import com.trcolgrove.contours.R;
import com.trcolgrove.contours.util.DrawingUtils;

/**
 * Created by Thomas on 9/1/15.
 */
public class ParallelogramTextView extends TextView {

    private int color;
    private int strokeColor;
    Path shapePath = new Path();
    Paint paint = new Paint();

    public ParallelogramTextView(Context context) {
        this(context, null, 0);
    }

    public ParallelogramTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ParallelogramTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.parallelogram, 0, 0);
            color = a.getColor(R.styleable.parallelogram_bg_color, color);
            strokeColor = DrawingUtils.lighter(color, 0.7);
            a.recycle();
        }
        paint.setAntiAlias(true);
        paint.setStrokeWidth(5);
    }

    @Override
    public void draw(Canvas canvas) {

        shapePath.moveTo(0, getHeight());
        shapePath.lineTo(getWidth() - getPaddingRight(), getHeight());
        shapePath.lineTo(getWidth(), 0);
        shapePath.lineTo(getPaddingLeft(), 0);
        shapePath.lineTo(0, getHeight());
        shapePath.close();

        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(shapePath, paint);

        paint.setColor(DrawingUtils.lighter(color, 0.7));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(shapePath, paint);

        super.draw(canvas);
    }

}
