package com.trcolgrove.colorfulPiano;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.trcolgrove.contours.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

/**
 * Piano Widget
 * Provides a piano widget that can interface
 * with both touch control and midi input
 *
 * Although this implementation is pretty and colorful,
 * One can extend the WhitePianoKey or BlackPianoKey
 * classes and change the draw methods to change how it looks!
 *
 * This class heavily references an implementation
 * by 2bard
 * {link https://github.com/2bard/AndroidPianoView}
 *
 * @author Thomas Colgrove
 */
public class Piano extends View {

    private static final int defaultKeyCount = 2;
    private PianoKey[] pianoKeys;

    // ordered by white keys then black keys
    // for drawing and touch purposes
    private ArrayList<PianoKey> drawKeys;
    private TreeMap<Integer, Touch> touches;
    private int keyCount;

    private Rect drawingRect;
    private static final int bottom_note = 48;

    ArrayList<Integer> blackKeyIndexes = new ArrayList<>(Arrays.asList(1, 3, 6, 8, 10));

    public Piano(Context context) {
        this(context, null);
    }

    public Piano(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Piano(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        this.touches = new TreeMap<>();
        keyCount = defaultKeyCount;

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Piano, 0, 0);
            keyCount = a.getInt(R.styleable.Piano_key_count, defaultKeyCount);
            a.recycle();
        }

        drawingRect = new Rect();
        pianoKeys = new PianoKey[keyCount];

        drawKeys = new ArrayList<>();
        ArrayList<PianoKey> blackKeys = new ArrayList<>();


        for (int noteVal = 0; noteVal < this.keyCount; noteVal++) {
            if(isBlackKey(noteVal)) {
                PianoKey newKey = new BlackPianoKey(context, this, noteVal);
                pianoKeys[noteVal] = newKey;
                blackKeys.add(newKey);
            }
            else{
                PianoKey newKey = new WhitePianoKey(context, this, noteVal);
                pianoKeys[noteVal] = newKey;
                drawKeys.add(newKey);
            }
        }
        drawKeys.addAll(blackKeys);
    }

    private boolean isBlackKey(int note_val){
        return (blackKeyIndexes.contains(note_val%12));
    }

    private PianoKey getPianoKeyByMidiVal(int midiVal) {
        int keyIndex = midiVal - bottom_note;
        PianoKey key;
        try{
            key = pianoKeys[keyIndex];
        } catch(ArrayIndexOutOfBoundsException ex) {
            System.err.print("error: key press out of bounds, " +
                    "attempting to resolve");
            keyIndex = (midiVal%(24));
            key = pianoKeys[keyIndex];
        }
        return key;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        getDrawingRect(drawingRect);
        for (int i = 0; i < drawKeys.size(); ++i) {
                drawKeys.get(i).layout(drawingRect, keyCount);
        }
        for (int i = 0; i < drawKeys.size(); ++i) {
                drawKeys.get(i).draw(canvas);
        }
        this.setOnTouchListener(new KeyPressListener(touches));
        canvas.drawLine(0, 0, getWidth(), 0, new Paint(Color.WHITE));
    }

    public void noteOn(int midiVal, int velocity) {
        PianoKey key = getPianoKeyByMidiVal(midiVal);
        key.press(midiVal);
    }

    public void noteOff(int midiVal, int velocity){
        PianoKey key = getPianoKeyByMidiVal(midiVal);
        key.unpress();
    }

    public interface PianoKeyListener{
        public void keyPressed(int id, int action);
    }

    private class KeyPressListener implements OnTouchListener {
        private TreeMap<Integer, Touch> touches;

        public KeyPressListener(TreeMap<Integer, Touch> touches) {
            this.touches = touches;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            int action = event.getActionMasked();

            switch (action) {
                case MotionEvent.ACTION_MOVE:
                    handleActionPointerMove(event);
                    break;
                case MotionEvent.ACTION_DOWN:
                    pushKeyDown(event);
                    break;
                case MotionEvent.ACTION_UP:
                    handleActionUp(event);
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    pushKeyDown(event);
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    handleActionUp(event);
                    break;
            }
            return true;
        }

        public void handleActionUp(MotionEvent event) {

            int pointer_index = event.getPointerId(event.getActionIndex());
            if (touches.containsKey(pointer_index)) {
                touches.get(pointer_index).lift(bottom_note);
                touches.remove(pointer_index);
            }
        }

        private void pushKeyDown(MotionEvent event) {
            final int action = event.getAction();
            final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
                    >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
            final int pointerId = event.getPointerId(pointerIndex);
            PianoKey key = isPressingKey(event.getX(pointerIndex), event.getY(pointerIndex));
            if (!touches.containsKey(pointerId) && key != null) {
                Touch touch = new Touch();
                touch.press(key, bottom_note + key.getNoteValue());
                touches.put(pointerId, touch);
            }
        }

        private PianoKey isPressingKey(float xpos,float ypos){
            PianoKey pressing_key;

            if((pressing_key = isPressingKeyInSet(xpos, ypos)) != null){
                return pressing_key;
            } else {
                return isPressingKeyInSet(xpos, ypos);
            }
        }

        private PianoKey isPressingKeyInSet(float xpos,float ypos){
            for(int i = drawKeys.size() - 1; i >=0; i--) {
                PianoKey key = drawKeys.get(i);
                if(key.getBounds().contains((int) xpos, (int) ypos)){
                    return key;
                }
            }
            return null;
        }

        public void handleActionPointerMove(MotionEvent event) {
            for(int i = 0; i < event.getPointerCount(); i++){
                handlePointerIndex(i, event);
            }
        }

        private void handlePointerIndex(int index, MotionEvent event){
            int pointer_id = event.getPointerId(index);
            //has it moved off a key?
            PianoKey key = isPressingKey(event.getX(index), event.getY(index));
            Touch touch = touches.get(pointer_id);
            if(key == null && touch != null){
                touch.lift(60);
            } else if(touch != null && !touch.isPressing(key)){
                touch.lift(60);
                touch.press(key, bottom_note + key.getNoteValue());
            }

        }
    }
}



