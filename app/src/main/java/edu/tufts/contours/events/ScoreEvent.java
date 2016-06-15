package edu.tufts.contours.events;

/**
 * Created by Thomas on 7/14/15.
 */
public class ScoreEvent {

    public int totalScore;
    public int multiplier;
    public int scoreIncrement;
    public boolean contourComplete;

    public ScoreEvent(int totalScore, int multiplier, int scoreIncrement, boolean contourComplete) {
        this.totalScore = totalScore;
        this.multiplier = multiplier;
        this.scoreIncrement = scoreIncrement;
        this.contourComplete = contourComplete;
    }
}
