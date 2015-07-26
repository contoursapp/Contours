package com.trcolgrove.contours.contoursGame;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.IntDef;
import android.view.animation.LinearInterpolator;

import com.trcolgrove.contours.util.DrawingUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class representing a single note instance
 * 
 * Created by Thomas on 5/27/15.
 */
public class Note {

    @IntDef({B_SHARP, C, C_SHARP, D_FLAT, D, D_SHARP, E_FLAT, E, F_FLAT, E_SHARP, F, F_SHARP,
            G_FLAT, G, G_SHARP, A_FLAT, A, A_SHARP, B_FLAT, B, C_FLAT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface NoteName {}

    public static final int B_SHARP = 0;
    public static final int C       = 1;
    public static final int C_SHARP = 2;
    public static final int D_FLAT  = 3;
    public static final int D       = 4;
    public static final int D_SHARP = 5;
    public static final int E_FLAT  = 6;
    public static final int E       = 7;
    public static final int F_FLAT  = 8;
    public static final int E_SHARP = 9;
    public static final int F       = 10;
    public static final int F_SHARP = 11;
    public static final int G_FLAT  = 12;
    public static final int G       = 13;
    public static final int G_SHARP = 14;
    public static final int A_FLAT  = 15;
    public static final int A       = 16;
    public static final int A_SHARP = 17;
    public static final int B_FLAT  = 18;
    public static final int B       = 19;
    public static final int C_FLAT  = 20;

    //unused for now but may be helpful for later implementation
    private int noteVal;
    private int octave;

    private int midiValue;
    private int scaleDegree;

    public ContoursGameView getParent() {
        return parent;
    }

    private ContoursGameView parent;

    public int getRippleRadius() {
        return rippleRadius;
    }

    public void setRippleRadius(int rippleRadius) {
        this.rippleRadius = rippleRadius;
    }

    private int rippleRadius = 0;

    public int getRippleAlpha() {
        return rippleAlpha;
    }

    public void setRippleAlpha(int rippleAlpha) {
        this.rippleAlpha = rippleAlpha;
    }

    private int rippleAlpha = 0xFF;
    private int alpha = 0x00;
    private int xPos = -1;
    private int yPos = -1;
    private int radius = -1;
    private Paint notePaint;
    private Paint ripplePaint;
    private Paint cursorPaint;

    private int color;

    private static final Map<Integer, Integer> noteScaleDegs = Collections.unmodifiableMap(
            new TreeMap<Integer, Integer>() {{
                put(C_FLAT, 0);
                put(C, 0);
                put(C_SHARP, 0);
                put(D_FLAT, 1);
                put(D, 1);
                put(D_SHARP, 1);
                put(E_FLAT, 2);
                put(E, 2);
                put(E_SHARP, 2);
                put(F_FLAT, 3);
                put(F, 3);
                put(F_SHARP, 3);
                put(G_FLAT, 4);
                put(G, 4);
                put(G_SHARP, 4);
                put(A_FLAT, 5);
                put(A, 5);
                put(A_SHARP, 6);
                put(B_FLAT, 6);
                put(B, 6);
                put(B_SHARP, 7);
            }});

    private static final Map<Integer, Integer> noteScalePosition = Collections.unmodifiableMap(
            new TreeMap<Integer, Integer>() {{
                put(C_FLAT, 11);
                put(C, 0);
                put(C_SHARP, 1);
                put(D_FLAT, 1);
                put(D, 2);
                put(D_SHARP, 3);
                put(E_FLAT, 3);
                put(E, 4);
                put(E_SHARP, 5);
                put(F_FLAT, 4);
                put(F, 5);
                put(F_SHARP, 6);
                put(G_FLAT, 6);
                put(G, 7);
                put(G_SHARP, 8);
                put(A_FLAT, 8);
                put(A, 9);
                put(A_SHARP, 10);
                put(B_FLAT, 10);
                put(B, 11);
                put(B_SHARP, 0);
            }});


    public Note(Context context, @NoteName int noteName, int octave, ContoursGameView parent) throws InvalidNoteException {
        if(octave < -2 || octave > 8) {
            throw new InvalidNoteException("note falls outside of acceptable octave range");
        }
        this.octave = octave;
        this.noteVal = noteName;
        this.midiValue = 24 + (octave * 12) + noteScalePosition.get(noteName);
        this.scaleDegree = noteScaleDegs.get(noteName);
        this.notePaint = new Paint();
        notePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        ripplePaint = new Paint();
        this.color = context.getResources().getColor(DrawingUtils.keyColors[scaleDegree]);
        this.parent = parent;
        this.cursorPaint = new Paint();

        cursorPaint.setStrokeWidth(4);
        cursorPaint.setColor(Color.WHITE);
        cursorPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        cursorPaint.setAntiAlias(true);
    }

    public void layout(int x, int y, int radius) {
        this.xPos = x;
        this.yPos = y;
        this.radius = radius;
    }

    public boolean inBounds(Point p) {
        if((p.x > (xPos-radius)) && (p.x < (xPos+radius))
                && (p.y > (yPos-radius)) && (p.y < (yPos+radius))) {
            return true;
        }
        return false;
    }

    public int getColor() {
        return color;
    }

    public void draw(Canvas canvas, boolean isSelected) throws LayoutException {
        if(xPos == -1 || yPos == -1 || radius == -1) {
            throw new LayoutException("Error: attempted to draw note without laying it out");
        }

        notePaint.setColor(color);

        if(isSelected) {
            DrawingUtils.drawTriangle(canvas, new Point(xPos - radius, yPos - (radius * 2)),
                    new Point(xPos, yPos - radius),
                    new Point(xPos + radius, yPos - (radius * 2)), cursorPaint);
        }

        canvas.drawCircle(xPos, yPos, radius, notePaint);
        notePaint.setColor(DrawingUtils.lighter(color, 0.7));

        canvas.drawCircle(xPos, yPos, ((radius * 2) / 3), notePaint);

        drawRipple(canvas);
    }

    /**
     * This function is called when the note is played when it is selected,
     * i.e. a Note "hit"
     */
    public void hit() {
        startRipple();
    }

    private void startRipple() {
        ValueAnimator rippleRadiusAnim = ObjectAnimator.ofInt(this, "rippleRadius", radius, 300);
        rippleRadiusAnim.setInterpolator(new LinearInterpolator());
        rippleRadiusAnim.setDuration(500);
        rippleRadiusAnim.start();
        ValueAnimator rippleAlphaAnim = ObjectAnimator.ofInt(this, "rippleAlpha", 255, 0);
        rippleAlphaAnim.setInterpolator(new LinearInterpolator());
        rippleAlphaAnim.setDuration(500);
        rippleAlphaAnim.start();
    }

    public void drawRipple(Canvas canvas) {
        ripplePaint.setStrokeWidth(2);
        ripplePaint.setStyle(Paint.Style.STROKE);
        ripplePaint.setColor(color);
        ripplePaint.setAlpha(rippleAlpha);
        canvas.drawCircle(xPos, yPos, rippleRadius, ripplePaint);
    }

    public int getMidiValue() {
        return midiValue;
    }

    public int getScaleDegree() { return this.scaleDegree; }

    public int getxPos() {
        return xPos;
    }

    public int getyPos() {
        return yPos;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }
}
