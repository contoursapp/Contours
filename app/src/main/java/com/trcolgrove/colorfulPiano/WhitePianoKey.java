package com.trcolgrove.colorfulPiano;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;

/**
 * Created by Thomas on 5/25/15.
 */
public class WhitePianoKey extends PianoKey {

    //TODO: make a ColorfulWhiteKey to extend this....

    public WhitePianoKey(Context context, Piano piano, int noteValue){
        super(context, piano, noteValue);
    }

    @Override
    public void layout(Rect drawingRect, int keyCount){

        int whiteKeyWidth = getWhiteKeyWidth(drawingRect, keyCount);
        int blackKeyWidth = getBlackKeyWidth(drawingRect, keyCount);

        colorRect.top = 0;
        colorRect.bottom = getWhiteKeyHeight(drawingRect);
        colorRect.left = (((noteValue/12)*7) + key_positions.get(noteValue%12)) *
                whiteKeyWidth;
        colorRect.right = colorRect.left + whiteKeyWidth;

        mainRect.top = colorRect.top + outline_width;
        mainRect.bottom = colorRect.bottom - outline_width;
        mainRect.left = colorRect.left + outline_width;
        mainRect.right = colorRect.right - outline_width;
    }

    @Override
    public void draw(Canvas canvas){
        strokePaint.setColor(Color.BLACK);

        if (getTouches()) {
            fillPaint.setColor(darkColor);
        } else {
            fillPaint.setColor(color);
        }

        canvas.drawRect(colorRect, fillPaint);
        canvas.drawRect(colorRect, strokePaint);

        if (getTouches()) {
            fillPaint.setColor(Color.LTGRAY);
        } else {
            fillPaint.setColor(Color.WHITE);
        }
        canvas.drawRect(mainRect, fillPaint);
    }
}
