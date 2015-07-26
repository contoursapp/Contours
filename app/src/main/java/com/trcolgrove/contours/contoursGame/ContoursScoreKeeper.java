package com.trcolgrove.contours.contoursGame;

import android.os.SystemClock;
import android.support.annotation.IntDef;

import com.trcolgrove.contours.events.ScoreEvent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.greenrobot.event.EventBus;

/**
 * Created by Thomas on 7/14/15.
 */
public class ContoursScoreKeeper implements ScoreKeeper {

    private int score;
    private int multiplier;
    private long baseTime;

    @IntDef({NOTE_HIT, NOTE_MISS, CONTOUR_COMPLETE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GameEvent {}
    public static final int NOTE_MISS = 0;
    public static final int NOTE_HIT = 1;
    public static final int CONTOUR_COMPLETE = 2;

    public ContoursScoreKeeper(long baseTime) {
        this.score = 0;
        this.multiplier = 1;
        this.baseTime = baseTime;
    }

    public void updateScore(@GameEvent int gameInfo) {
        int scoreIncrement = 0;
        if(gameInfo == NOTE_HIT) {
            scoreIncrement = noteHit();
            score += scoreIncrement;
        } else if(gameInfo == NOTE_MISS) {
            scoreIncrement = noteMiss();
            score += scoreIncrement;
        } else if(gameInfo == CONTOUR_COMPLETE) {
            scoreIncrement = contourComplete();
            score += scoreIncrement;
        }
        EventBus.getDefault().post(new ScoreEvent(score, multiplier, scoreIncrement));
    }

    private int noteMiss() {
        multiplier = 1;
        return -20;
    }

    private int noteHit() {
        int scoreIncrement = (100 * multiplier);
        incrementMultiplier();
        return scoreIncrement;
    }

    private int contourComplete() {
        int scoreIncrement = (int)(2000/((SystemClock.elapsedRealtime() - baseTime) / 1000));
        incrementMultiplier();
        return scoreIncrement;
    }
    private void incrementMultiplier() {
        if(multiplier < 10) {
            multiplier++;
        }
    }

    public int getScore() {
        return this.score;
    }

    public int getMultiplier() {
        return this.multiplier;
    }

}
