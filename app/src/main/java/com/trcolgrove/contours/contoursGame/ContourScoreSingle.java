package com.trcolgrove.contours.contoursGame;

/**
 * Created by Thomas on 3/4/16.
 */
public class ContourScoreSingle {

    private int contourID;
    private long completionTime;
    private int mistakes;
    private int interOnsetIntervalStdDev;

    public int getContourID() {
        return contourID;
    }

    public void setContourID(int contourID) {
        this.contourID = contourID;
    }

    public long getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(long completionTime) {
        this.completionTime = completionTime;
    }

    public int getMistakes() {
        return mistakes;
    }

    public void setMistakes(int mistakes) {
        this.mistakes = mistakes;
    }

    public int getInterOnsetIntervalStdDev() {
        return interOnsetIntervalStdDev;
    }

    public void setInterOnsetIntervalStdDev(int interOnsetIntervalStdDev) {
        this.interOnsetIntervalStdDev = interOnsetIntervalStdDev;
    }

}
