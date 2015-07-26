package com.trcolgrove.contours.events;

/**
 * Created by Thomas on 7/14/15.
 */
public class ScoreEvent {

    public int totalScore;
    public int multiplier;
    public int scoreIncrement;

    public ScoreEvent(int totalScore, int multiplier, int scoreIncrement) {
        this.totalScore = totalScore;
        this.multiplier = multiplier;
        this.scoreIncrement = scoreIncrement;
    }
}
