package com.trcolgrove.contours.contoursGame;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.trcolgrove.contours.R;
import com.trcolgrove.contours.accessors.NoteAccessor;
import com.trcolgrove.contours.util.DrawingUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

/**
 * Main Game View for an instance of the Contours training application
 *
 * Created by Thomas on 5/19/15.
 */
public class ContoursGameView extends SurfaceView {

    private static String TAG = "ContoursGameView";

    private int staffMargin = DrawingUtils.dpToPixels(32, getContext());

    private Drawable[] octaveDividerDrawables = {getResources().getDrawable(R.drawable.octave_1), getResources().getDrawable(R.drawable.octave_2),
            getResources().getDrawable(R.drawable.octave_3)};


    //object to keep track of score, multiplier and other performance data
    private static ScoreKeeper scoreKeeper;


    private int congratsTextAlpha = 0;
    private int noteAlpha = 255;

    // midi-poisition mapping. For now only C major supported. Essentially this is a util to
    // figure out where on the staff each midi note should map... needs more robust implementation
    // if we decide to support accidentals
    private static final int[] noteValToPosition = {0, 0, 1, 1, 2, 3, 3, 4, 4, 5, 5, 6};
    private static final String[] congratsMessages = {"Good Job!", "Rock On!", "Excellent!",
            "BOOM", "Awesome!" , "Woo!", "Great Work!", "SUCCESS!"};
    private static final String[] failureMessages = {"Try Again", "Whoops!", "Oops"};

    private static final int transitionMilis = 2500;
    private String gameUpdateText = "dope";
    // a direct map from midiValue to staff location, with the bottom line being position 0,
    // this is important because often multiple notes map to the same staff location, ie C and Csharp
    protected static Map<Integer, Integer> midiValToStaffLoc;
    private int spaceHeight; //the height of space between each staff line
    private Contour contour;


    //default staff values
    private static final int defaultBottomNote = 48;
    private static final int defaultTopNote = 84;

    //Staff properties
    private static final int notesDisplayedOnScreen = 22; //number of notes displayed by one screen w/o scroll
    private int staffPositionCount;
    private int scrollOffsetY;
    private int contourIndex = 0;
    private int bottomMidiNote;
    private int topMidiNote;
    private static final int lineStrokeWidth = 3;
    private Paint staffPaint = new Paint();
    private Paint textPaint = new Paint();
    private Paint keySelectPaint = new Paint();
    private final Rect textBounds = new Rect();

    private TweenManager tweenManager;
    //using the tween library instead of animators so I dont have to invoke the uithread?

    private List<Contour> contours; //The contours for this particular game activity

    private GameLoopThread gameLoopThread; //Game Loop

    private boolean transitioning = false; //indicates whether or not we are transitioning between contours

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

        tweenManager = new TweenManager();
        Tween.registerAccessor(Note.class, new NoteAccessor());

        initGameLoop();
        initStaff();

        String[] contourStrings = getResources().getStringArray(R.array.contours);
        contours = ContourFactory.getContoursFromStringArray(contourStrings, context);

        this.scoreKeeper = new ContoursScoreKeeper(SystemClock.elapsedRealtime());
        this.setDrawingCacheEnabled(true);
        this.buildDrawingCache();

        setContour(contours.get(contourIndex));
    }


    public void resetContourOnFailure() {
        Random rand = new Random();
        int msgIndex = rand.nextInt(failureMessages.length);
        gameUpdateText = failureMessages[msgIndex];
        ValueAnimator textAnim = ObjectAnimator.ofInt(this, "congratsTextAlpha", 0, 255, 0);
        textAnim.setDuration(transitionMilis);
        textAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        transitioning = true;
        textPaint.setColor(Color.RED);

        textAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                transitioning = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        textAnim.start();
    }


    /**
     * This function is called when the player completes a contour
     * Instantiates the next contour in the sequence and calls other
     * animations and effects.
     */
    public void nextContour() {
        transitioning = true;

        textPaint.setColor(Color.WHITE);
        Random rand = new Random();
        int msgIndex = rand.nextInt(congratsMessages.length);
        gameUpdateText = congratsMessages[msgIndex];
        AnimatorSet transitionAnims = new AnimatorSet();

        ValueAnimator textAnim = ObjectAnimator.ofInt(this, "congratsTextAlpha", 0, 255, 0);
        textAnim.setDuration(transitionMilis);
        ValueAnimator noteAnimOut = ObjectAnimator.ofInt(this, "noteAlpha", 255, 0);
        noteAnimOut.setDuration(transitionMilis / 2);
        ValueAnimator noteAnimIn = ObjectAnimator.ofInt(this, "noteAlpha", 0, 255);
        noteAnimIn.setDuration(transitionMilis/2);

        transitionAnims.setInterpolator(new AccelerateDecelerateInterpolator());
        transitionAnims.playTogether(textAnim, noteAnimOut);
        transitionAnims.play(noteAnimIn).after(noteAnimOut);

        noteAnimOut.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                contourIndex++;
                setContour(contours.get(contourIndex));
                transitioning = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        transitionAnims.start();
    }

    /**
     * initialize the main game loop
     */
    private void initGameLoop(){
        gameLoopThread = new GameLoopThread(this, tweenManager);
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
                        e.printStackTrace();
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

    /**
     * Initialize the staff's properties. Specifically,
     * create the mapping from midiValue to location on the staff,
     * based on the bottomMidiNote and the topMidiNote
     */
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

    /* Set the game screen's contour */
    public void setContour(Contour contour) {
        this.contour = contour;
    }

    /**
     * Scroll the staff to the offset defined by newScrollOffsetY
     * While the new ui does not scroll, Im keeping this method for possible future use.
     * @param newScrollOffsetY the offset to which the staff must be scrolled
     */
    private void scrollStaff(int newScrollOffsetY, boolean animate) {
        if(animate) {
            ValueAnimator scrollAnimator = ObjectAnimator.ofInt(this, "scrollOffsetY", scrollOffsetY, newScrollOffsetY);
            int duration = Math.abs(newScrollOffsetY - scrollOffsetY);
            scrollAnimator.setDuration(duration);
            scrollAnimator.start();
        } else {
            this.scrollOffsetY = newScrollOffsetY;
        }
    }

    /**
     * Process midi input from the midikeyboard or a touch event
     * Compares midiValue to the midiValue of the selected note in the contour
     * and updates the game state accordingly
     * @param midiValue the midiValue being compared to
     * @return true if the user has completed the activity
     */
    public boolean processMidiInput(int midiValue){

        if(transitioning) {
            scoreKeeper.updateScore(ContoursScoreKeeper.NOTE_MISS);
            return false;
        }

        List<Note> notes = contour.getNotes();
        Note first = notes.get(contour.getCursorPosition());
        if(first.getMidiValue() ==  midiValue){
            if((notes.size() - 1) == contour.getCursorPosition()) {
                scoreKeeper.updateScore(ContoursScoreKeeper.CONTOUR_COMPLETE);
                if(contourIndex < contours.size()-1) {
                    nextContour();
                } else {
                    gameLoopThread.setRunning(false);
                    gameLoopThread.interrupt();
                    return true;
                }
            } else {
                scoreKeeper.updateScore(ContoursScoreKeeper.NOTE_HIT);
                contour.incrementCursorPosition();
                first.hit(tweenManager);
            }
        } else {
            resetContourOnFailure();
            contour.setCursorPosition(0);
            scoreKeeper.updateScore(ContoursScoreKeeper.NOTE_MISS);
        }
        return false;
    }

    /** Retrieve the y coordinate of a specific staffPosition, with position
     * 0 indicating the bottom of the staff.
     * @param staffPosition a position on the staff. For example '0' is the bottom line
     *                      of the staff. The next space is 1, and the next line is 2.
     * @return a y coordinate indicating the vertical placement of staffPosition on the screen
     */
    private int getStaffPositionYCoordinate(int staffPosition) {
        return ((getHeight()) - ((staffPosition*(spaceHeight/2)))) - staffMargin;
    }

    /* for use with scrolling staff implementation.
    * At present the scrolling staff implementation has been nixed.
    * I'm keeping this method if we want to reintroduce this ui component.
    */
    private int getScrolledStaffPositionYCoordinate(int staffPosition) {
        return getStaffPositionYCoordinate(staffPosition) + scrollOffsetY;
    }

    /**
     *
     * @return the x coordinate of the specified key
     */
    private int getKeyXCoordinate(int staffPosition) {
        int keyWidth = getWidth()/staffPositionCount;
        return keyWidth * staffPosition + (keyWidth/2);
    }


    /* Main drawing functions */
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawARGB(255, 0, 0, 0);
        Drawable bg = getResources().getDrawable(R.drawable.gradient_background);
        bg.setBounds(0, 0, getWidth(), getHeight());
        bg.draw(canvas);

        drawOctaveDividers(canvas);
        drawStaff(canvas);

        try {
            drawContour(canvas);
        } catch (LayoutException e) {
            e.printStackTrace();
        }
        drawCongratsText(canvas);

        if(contour.getCursorPosition() == 0) {
            keySelectPaint.setColor(getResources().getColor(R.color.red));
            keySelectPaint.setAlpha(noteAlpha);
            keySelectPaint.setStyle(Paint.Style.FILL);
            Note firstNote = contour.getNotes().get(0);
            int midiValue = firstNote.getMidiValue();
            int staffLoc = midiValToStaffLoc.get(midiValue);
            int keySelectorX = getKeyXCoordinate(staffLoc);
            int triangleOffset = spaceHeight/2;
            int arrowWidth = spaceHeight;
            int arrowHeight = spaceHeight/2;
            DrawingUtils.drawArrow(canvas,
                    keySelectorX,
                    getHeight(),
                    arrowHeight,
                    arrowWidth,
                    keySelectPaint
            );
        }
    }

    private void drawCongratsText(Canvas canvas) {
        int x = canvas.getWidth()/2;
        int y = getStaffPositionYCoordinate(notesDisplayedOnScreen/2);

        textPaint.getTextBounds(gameUpdateText, 0, gameUpdateText.length(), textBounds);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(225f);
        textPaint.setAlpha(congratsTextAlpha);

        textPaint.setShadowLayer(25f, 10, 10, Color.BLACK);
        canvas.drawText(gameUpdateText, x, y - textBounds.exactCenterY(), textPaint);
    }

    private void drawOctaveDividers(Canvas canvas) {
        for(int i = 0; i < 3; i++) {
            int yValBottom = getStaffPositionYCoordinate(i*7);
            int yValTop = getStaffPositionYCoordinate((i+1)*7);
            Drawable octaveDrawable = octaveDividerDrawables[i];
            octaveDrawable.setAlpha(150);
            octaveDrawable.setBounds(0, yValTop, getWidth(), yValBottom);
            octaveDrawable.draw(canvas);
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
        int noteRadius = spaceHeight/2;
        int noteXPos = noteSpacing;

        for(int i = 0; i < notes.size(); i++){
            Note note = notes.get(i);
            note.setAlpha(noteAlpha);
            boolean isSelected = false;
            if(i == contour.getCursorPosition()) {
                isSelected = true;
            }
            int notePostion = midiValToStaffLoc.get(note.getMidiValue());
            int noteYPos = getStaffPositionYCoordinate(notePostion);
            note.layout(noteXPos, noteYPos, noteRadius);
            note.draw(canvas, isSelected);

            noteXPos += noteSpacing;
        }
    }

    private int getSpaceHeight() {
        return (getHeight()-staffMargin)/(notesDisplayedOnScreen/2);
    }

    /**
     * Render the staff on the canvas
     * @param canvas the canvas on which to draw the staff
     */
    private void drawStaff(Canvas canvas) {

        spaceHeight = getSpaceHeight();
        staffPaint.setStrokeWidth(lineStrokeWidth);
        staffPaint.setColor(Color.WHITE);

        for(int i = 0; i < staffPositionCount/2; i++) {
            int yVal = getStaffPositionYCoordinate(i * 2);
            canvas.drawLine(staffMargin, yVal, getWidth() - staffMargin, yVal, staffPaint);
        }
    }


    /* The following functions were used in the old ui as means of scrolling the staff
     * This is not relevant to the new ui as all the notes are displayed at the same time.
     * I am keeping these functions for now in case we need them again...
     */

    /* used to calculate the midpoint of a contour and scroll the staff accordingly
     * in scrolling implementation. Not currently used */
    private void setStaffYOffset(boolean animate) {
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

    public int getCongratsTextAlpha() {
        return congratsTextAlpha;
    }

    public void setCongratsTextAlpha(int congratsTextAlpha) {
        this.congratsTextAlpha = congratsTextAlpha;
    }

    public int getNoteAlpha() {
        return noteAlpha;
    }

    public void setNoteAlpha(int noteAlpha) {
        this.noteAlpha = noteAlpha;
    }
}
