package com.trcolgrove.contours.contoursGame;

/**
 * Created by Thomas on 3/4/16.
 */
public class ScoreSingle {

    private int contourId;
    private String difficulty;
    private int noteGap;
    private String sound;
    private int contourStartMidiNote;
    private long completionTime;
    private int notesHit;
    private int numberOfErrors;
    private double percentError;
    private long successDuration;
    private double interOnsetIntervalStdDev;

    public ScoreSingle(int contourId, String difficulty, int noteGap, String sound,
                       int contourStartMidiNote, long completionTime, int notesHit, int numberOfErrors,
                       double percentError, long successDuration, double interOnsetIntervalStdDev) {
        this.contourId = contourId;
        this.difficulty = difficulty;
        this.noteGap = noteGap;
        this.sound = sound;
        this.contourStartMidiNote = contourStartMidiNote;
        this.completionTime = completionTime;
        this.notesHit = notesHit;
        this.numberOfErrors = numberOfErrors;
        this.percentError = percentError;
        this.successDuration = successDuration;
        this.interOnsetIntervalStdDev = interOnsetIntervalStdDev;
    }

    public int getContourId() {
        return contourId;
    }

    public void setContourId(int contourId) {
        this.contourId = contourId;
    }

    public long getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(long completionTime) {
        this.completionTime = completionTime;
    }

    public int getNumberOfErrors() {
        return numberOfErrors;
    }

    public void setNumberOfErrors(int numberOfErrors) {
        this.numberOfErrors = numberOfErrors;
    }

    public double getInterOnsetIntervalStdDev() {
        return interOnsetIntervalStdDev;
    }

    public void setInterOnsetIntervalStdDev(double interOnsetIntervalStdDev) {
        this.interOnsetIntervalStdDev = interOnsetIntervalStdDev;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getNoteGap() {
        return noteGap;
    }

    public void setNoteGap(int noteGap) {
        this.noteGap = noteGap;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public int getContourStartMidiNote() {
        return contourStartMidiNote;
    }

    public void setContourStartMidiNote(int contourStartMidiNote) {
        this.contourStartMidiNote = contourStartMidiNote;
    }

    public double getPercentError() {
        return percentError;
    }

    public void setPercentError(double percentError) {
        this.percentError = percentError;
    }

    public long getSuccessDuration() {
        return successDuration;
    }

    public void setSuccessDuration(long successDuration) {
        this.successDuration = successDuration;
    }


    public int getNotesHit() {
        return notesHit;
    }

    public void setNotesHit(int notesHit) {
        this.notesHit = notesHit;
    }
}
