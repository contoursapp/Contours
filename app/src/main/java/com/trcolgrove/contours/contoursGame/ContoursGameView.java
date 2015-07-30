package com.trcolgrove.contours.contoursGame;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.trcolgrove.contours.R;
import com.trcolgrove.contours.util.DrawingUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main Game View for an instance of the Contours training application
 *
 * Created by Thomas on 5/19/15.
 */
public class ContoursGameView extends SurfaceView {

    private static String TAG = "ContoursGameView";


    boolean firstLoad = true;

    //object to keep track of score, multiplier and other performance data
    private static ScoreKeeper scoreKeeper;

    // midi-poisition mapping. For now only C major supported. Essentially this is a util to
    // figure out where on the staff each midi note should map... needs more robust implementation
    // if we decide to support accidentals
    protected static final int[] noteValToPosition = {0, 0, 1, 1, 2, 3, 3, 4, 4, 5, 5, 6};
    // protected static final String[] noteVals = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"}

    // a direct map from midiValue to staff location, with the bottom line being position 0,
    // this is important because often multiple notes map to the same staff location, ie C and Csharp
    protected static Map<Integer, Integer> midiValToStaffLoc;
    private int spaceHeight; //the height of space between each staff line
    private Contour contour;

    //default staff values
    private static final int defaultBottomNote = 48;
    private static final int defaultTopNote = 84;

    //Staff properties
    private static final int notesDisplayedOnScreen = 16; //number of notes displayed by one screen w/o scroll
    private int staffPositionCount;
    private int scrollOffsetY;
    private int contourIndex = 0;
    private int bottomMidiNote;
    private int topMidiNote;
    private static final int lineStrokeWidth = 5;
    private Paint staffPaint = new Paint();


    private List<Contour> contours; //The contours for this particular game activity

    private GameLoopThread gameLoopThread; //Game Loop

    public ContoursGameView(Context context) throws InvalidNoteException {
        this(context, null);
    }

    public ContoursGameView(Context context, AttributeSet attrs) throws InvalidNoteException {
        this(context, attrs, 0);
    }

    public ContoursGameView(Context context, AttributeSet attrs, int defStyle) throws InvalidNoteException {
        super(context, attrs, defStyle);

        setZOrderOnTop(true);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        scrollOffsetY = 0;

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ContoursGameView, 0, 0);
            topMidiNote = a.getInt(R.styleable.ContoursGameView_topNote, defaultTopNote);
            bottomMidiNote = a.getInt(R.styleable.ContoursGameView_bottomNote, defaultBottomNote);
            a.recycle();
        }

        initGameLoop();
        initStaff();

        String[] contourStrings = getResources().getStringArray(R.array.contours);
        contours = ContourFactory.getContoursFromStringArray(contourStrings, context);

        this.scoreKeeper = new ContoursScoreKeeper(SystemClock.elapsedRealtime());
    }

    /**
     * This function is called when the player completes a contour
     * Instantiates the next contour in the sequence and calls other
     * animations and effects.
     */
    private void nextContour() {
        contourIndex++;
        setContour(contours.get(contourIndex), true);
    }


    /**
     * initialize the main game loop
     */
    private void initGameLoop(){
        gameLoopThread = new GameLoopThread(this);
        SurfaceHolder holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                boolean retry = true;
                Log.d(TAG, "surface destroyed");
                gameLoopThread.setRunning(false);
                while (retry) {
                    try {
                        gameLoopThread.join();
                        retry = false;
                    } catch (InterruptedException e) {
                    }
                }
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                gameLoopThread.setRunning(true);
                gameLoopThread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format,
                                       int width, int height) {
            }
        });
    }

    private void initStaff() {
        midiValToStaffLoc = new HashMap<>();
        int staffPosition = 0;
        int prevNoteHeight = -1;
        staffPositionCount = 0;

        for(int i = bottomMidiNote; i <= topMidiNote; i++){
            int noteVal = i%12;
            int noteHeight = noteValToPosition[noteVal];

            midiValToStaffLoc.put(i, staffPosition);
            if(noteHeight != prevNoteHeight) {
                staffPosition++;
                staffPositionCount++;
                prevNoteHeight = noteHeight;
            }
        }
    }

    public void setContour(Contour contour, boolean animate) {
        this.contour = contour;

        int topNote = contour.getTopMidiVal();
        int topPosition = midiValToStaffLoc.get(topNote);

        int bottomNote = contour.getBottomMidiVal();
        int bottomPosition = midiValToStaffLoc.get(bottomNote);

        int midwayPosition = (topPosition + bottomPosition) / 2;
        int numSpacesFromBottom = midwayPosition / 2;

        int newStaffYOffset = ((getHeight()/2) + (numSpacesFromBottom * spaceHeight) - getHeight());
        scrollStaff(newStaffYOffset, animate);
    }

    //These are used by the ScrollAnimator do not remove!
    public void setScrollOffsetY(int offset) {
        this.scrollOffsetY = offset;
    }

    public int getScrollOffsetY() {
        return this.scrollOffsetY;
    }

    /**
     * Scroll the staff to the offset defined by newScrollOffsetY
     * @param newScrollOffsetY the offset to which the staff must be scrolled
     */
    private void scrollStaff(int newScrollOffsetY, boolean animate) {
        if(animate) {
            ValueAnimator scrollAnimator = ObjectAnimator.ofInt(this, "scrollOffsetY", scrollOffsetY, newScrollOffsetY);
            int duration = Math.abs(newScrollOffsetY - scrollOffsetY);
            scrollAnimator.setDuration(duration);
            scrollAnimator.start();
        } else {
            scrollOffsetY = newScrollOffsetY;
        }
    }

    public Note checkNote(int midiValue){
        List<Note> notes = contour.getNotes();

        Note first = notes.get(contour.getCursorPosition());
        if(first.getMidiValue() ==  midiValue){
            if((notes.size() - 1) == contour.getCursorPosition()) {
                nextContour();
                scoreKeeper.updateScore(ContoursScoreKeeper.CONTOUR_COMPLETE);
                return first;
            } else {
                scoreKeeper.updateScore(ContoursScoreKeeper.NOTE_HIT);
                first.hit();
                contour.updateCursor();
                return first;
            }
        } else {
            scoreKeeper.updateScore(ContoursScoreKeeper.NOTE_MISS);
        }

        return null;
    }

    private int getStaffPositionYCoordinate(int staffPosition) {
        return (getHeight() + scrollOffsetY) - ((staffPosition*(spaceHeight/2)));
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        //This is a necessary clause because setContour relies on the view having been loaded to work
        if(firstLoad) {
            setContour(contours.get(contourIndex), false);
            firstLoad = false;
        }
        canvas.drawARGB(255, 0, 0, 0);
        Drawable bg = getResources().getDrawable(R.drawable.gradient_background);
        bg.setBounds(0, getHeight() - spaceHeight * (staffPositionCount / 2) + scrollOffsetY, canvas.getWidth(),
                getHeight() + scrollOffsetY);
        bg.draw(canvas);



        drawStaff(canvas);

        try {
            drawContour(canvas);
        } catch (LayoutException e) {
            e.printStackTrace();
        }
    }

    /**
     * Draw the current contour on the canvas
     * @param canvas the canvas on which to draw the contour
     * @throws LayoutException
     */
    public void drawContour(Canvas canvas) throws LayoutException {
        List<Note> notes = contour.getNotes();
        int noteSpacing = getWidth()/(notes.size()+1);
        int radius = spaceHeight /2;
        // a little extra space for the front/back
        int noteXPos = noteSpacing;

        for(int i = 0; i < notes.size(); i++){

            Note note = notes.get(i);
            boolean isSelected = false;
            if(i == contour.getCursorPosition()) {
                isSelected = true;

            }
            int notePostion = midiValToStaffLoc.get(note.getMidiValue());
            int noteYPos = getStaffPositionYCoordinate(notePostion);
            note.layout(noteXPos, noteYPos, radius);
            note.draw(canvas, isSelected);

            noteXPos += noteSpacing;
        }
    }


    /**
     * Render the staff on the canvas
     * @param canvas the canvas on which to draw the staff
     */
    private void drawStaff(Canvas canvas) {

        spaceHeight = getHeight()/(notesDisplayedOnScreen/2);
        staffPaint.setColor(Color.WHITE); //TODO: make this customizable ?
        staffPaint.setStrokeWidth(lineStrokeWidth);

        for(int i = 0; i < staffPositionCount/2; i++) {
            int yVal = getStaffPositionYCoordinate(i*2);
            //drawText for debug only
            int staffLineMargin = DrawingUtils.dpToPixels(16, getContext());
            if((yVal < getHeight() - 40) && (yVal > 40) ) {
                canvas.drawLine(staffLineMargin, yVal, getWidth() - staffLineMargin, yVal, staffPaint);
            }
        }
    }

}
