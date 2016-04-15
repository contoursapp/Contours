package com.trcolgrove.contours.contoursGame;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.IntDef;

import com.google.gson.Gson;
import com.trcolgrove.contours.events.ScoreEvent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by Thomas on 7/14/15.
 */
public class ContoursScoreKeeper {

    private static final int transition_milis = 1500;
    private int score;
    private int multiplier;
    private long baseTime;
    private long contourStartTime;
    private long currentAttemptStartTime;
    private long lastNoteHitTime;

    private static final int BASE_SCORE = 100;

    private int totalNotesHit = 0;
    private int totalNotesMissed = 0;

    private int currentNotesHit = 0;
    private int currentNotesMissed = 0;

    private int streak = 0;
    private int longestStreak = 0;

    private int currentContourAttempts = 0;

    private String difficulty;
    private int intervalSize;
    private String sound;

    ArrayList<ScoreSingle> scoreSingles = new ArrayList<>();
    ArrayList<Long> indivNoteTimes = new ArrayList<>();

    @IntDef({NOTE_HIT, NOTE_MISS, CONTOUR_COMPLETE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GameEvent {}
    public static final int NOTE_MISS = 0;
    public static final int NOTE_HIT = 1;
    public static final int CONTOUR_COMPLETE = 2;

    public ContoursScoreKeeper(long baseTime, String difficulty, int intervalSize, String sound) {
        this.score = 0;
        this.multiplier = 1;
        this.baseTime = baseTime;
        this.difficulty = difficulty;
        this.intervalSize = intervalSize;
        this.sound = sound;
        contourStartTime = baseTime;
        currentAttemptStartTime = baseTime;
        lastNoteHitTime = baseTime;
    }

    public void noteMiss() {
        multiplier = 1;
        totalNotesMissed++;
        currentNotesMissed++;

        if(streak > longestStreak) {
            longestStreak = streak;
        }

        streak = 0;
        indivNoteTimes.clear();
        currentAttemptStartTime = SystemClock.elapsedRealtime();

        EventBus.getDefault().post(new ScoreEvent(score, multiplier, 0, false));
    }

    public void noteHit() {
        long currentTime = SystemClock.elapsedRealtime();
        long indivNoteHitTime = currentTime - lastNoteHitTime;
        lastNoteHitTime = currentTime;
        indivNoteTimes.add(indivNoteHitTime);

        totalNotesHit++;
        currentNotesHit++;
        streak++;
    }

    public void contourComplete(Contour contour) {
        noteHit();

        long totalContourTime = (SystemClock.elapsedRealtime() - contourStartTime - transition_milis);
        int timeBonus = BASE_SCORE - (int)(totalContourTime/250);
        int scoreIncrement = Math.max(0, BASE_SCORE + timeBonus) * multiplier;
        incrementMultiplier();
        contourStartTime = SystemClock.elapsedRealtime();
        score += scoreIncrement;
        EventBus.getDefault().post(new ScoreEvent(score, multiplier, scoreIncrement, true));

        double percentError = (double)currentNotesMissed/(currentNotesHit + currentNotesMissed) * 100;

        scoreSingles.add(new ScoreSingle(contour.getId(), difficulty, intervalSize, sound, contour.getNotes().get(0).getMidiValue(),
                totalContourTime, currentNotesMissed, percentError, sum(indivNoteTimes),
                calculateStandardDev(indivNoteTimes)));
        currentNotesHit = 0;
        currentNotesMissed = 0;
    }

    private void incrementMultiplier() {
        if(multiplier < 8) {
            multiplier++;
        }
    }

    private double calculateStandardDev(ArrayList<Long> values) {
        long avg = 0;
        for (Long value : values) {
            avg += value;
        }
        avg /= values.size();
        long variance = 0;
        for (Long value : values) {
            variance += Math.pow(value - avg, 2);
        }
        variance /= (values.size() - 1);

        return Math.sqrt(variance);
    }

    private long sum(ArrayList<Long> values) {
        long sum = 0;
        for(Long val : values) {
            sum += val;
        }
        return sum;
    }

    public int getScore() {
        return this.score;
    }

    public int getMultiplier() {
        return this.multiplier;
    }

    public int getTotalNotesHit() {
        return totalNotesHit;
    }

    public int getTotalNotesMissed() {
        return totalNotesMissed;
    }

    public int getStreak() {
        return streak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public int getAverageStreak() {
        return ((totalNotesHit + totalNotesMissed)/(totalNotesMissed +1));
    }

    /**
     * Returns the information kept in the ScoreKeeper object
     * as a Map. For example, the map might contain
     * the pair "total_score" -> 9000(int) "total_time"->3.56(Duration)
     *
     * @return a map representing the score keepers metrics and their values
     */
    public Bundle getScoreBundle() {
        Bundle scoreBundle = new Bundle();
        Gson gson = new Gson();

        scoreBundle.putInt("average_streak", getAverageStreak());
        scoreBundle.putInt("total_score", score);
        scoreBundle.putLong("total_time", SystemClock.elapsedRealtime() - baseTime);
        scoreBundle.putInt("longest_streak", longestStreak);
        scoreBundle.putInt("notes_hit", totalNotesHit);
        scoreBundle.putInt("notes_missed", totalNotesMissed);
        scoreBundle.putString("indiv_contour_info", gson.toJson(scoreSingles));
        scoreBundle.putInt("interval_size", intervalSize);
        scoreBundle.putString("difficulty", difficulty);
        scoreBundle.putInt("intervalSize", intervalSize);
        scoreBundle.putString("sound", sound);
        return scoreBundle;
    }

}
