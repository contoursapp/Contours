package com.trcolgrove.contours.contoursGame;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * The GameRunner class handles underlying
 * game progression and logic.
 *
 *
 * Created by Thomas on 7/11/15.
 */
public class GameRunner {
    //TODO: implement this

    private ContoursGameView gameContoursGameView;
    private ScoreKeeper scorer;
    private List<Contour> contours;
    private Difficulty difficulty;


    @IntDef({BEGINNER, INTERMEDIATE, VETERAN, EXPERT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Difficulty {}

    public static final int BEGINNER = 0;
    public static final int INTERMEDIATE = 1;
    public static final int VETERAN = 2;
    public static final int EXPERT  = 3;

    /**
     * GameRunner constructor
     */
    public GameRunner(Difficulty difficulty) {
        this.difficulty = difficulty;
    }


    public void startGame(int numContours) {

    }

    private void checkNote(int midiVal) {

    }


}
