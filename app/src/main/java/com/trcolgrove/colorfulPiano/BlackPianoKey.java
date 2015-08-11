package com.trcolgrove.colorfulPiano;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;

/**
 * Created by Thomas on 5/25/15.
 */
public class BlackPianoKey extends PianoKey {

    public BlackPianoKey(Context context, Piano piano, int noteValue){
        super(context, piano, noteValue);
    }

    @Override
    public void layout(Rect drawingRect, int keyCount){
        int whiteKeyWidth = getWhiteKeyWidth(drawingRect, keyCount);
        int blackKeyWidth = getBlackKeyWidth(drawingRect, keyCount);

        colorRect.top = 0;
        colorRect.bottom = getBlackKeyHeight(drawingRect);
        colorRect.left = (((noteValue/12)*7) + key_positions.get(noteValue%12) + 1) *
                whiteKeyWidth - (blackKeyWidth/2);
        colorRect.right = colorRect.left + blackKeyWidth;

        mainRect.top = colorRect.top;
        mainRect.bottom = colorRect.bottom;
        mainRect.left = colorRect.left;
        mainRect.right = colorRect.right;
    }

    @Override
    public void draw(Canvas canvas){
        strokePaint.setColor(Color.BLACK);

        if (getTouches()) {
            fillPaint.setColor(Color.DKGRAY);
        } else {
            fillPaint.setColor(Color.BLACK);
        }

        canvas.drawRect(colorRect, fillPaint);
        canvas.drawRect(colorRect, strokePaint);
    }

}
