package com.trcolgrove.contours.contoursGame;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Thomas on 6/25/15.
 */
public class Contour {

    private List<Note> notes;
    private ContourCursor cursor;
    private int topMidiVal;
    private int bottomMidiVal;

    public Contour(Note... notes) {
        int bottomNote = 128;
        int topNote = -1;
        for(Note note : notes) {
            if(note.getMidiValue() > topNote) {
                topNote = note.getMidiValue();
            }
            if(note.getMidiValue() < bottomNote) {
                bottomNote = note.getMidiValue();
            }
        }

        this.topMidiVal = topNote;
        this.bottomMidiVal = bottomNote;
        this.notes = Arrays.asList(notes);
        this.cursor = new ContourCursor(0);
    }

    //TODO: clean this class
    public Contour(List<Note> notes) {
        int bottomNote = 128;
        int topNote = -1;
        for(Note note : notes) {
            if(note.getMidiValue() > topNote) {
                topNote = note.getMidiValue();
            }
            if(note.getMidiValue() < bottomNote) {
                bottomNote = note.getMidiValue();
            }
        }

        this.topMidiVal = topNote;
        this.bottomMidiVal = bottomNote;
        this.notes = notes;
        this.cursor = new ContourCursor(0);
    }

    public List<Note> getNotes() { return notes; }

    public void updateCursor() {
        cursor.incrementPosition();
    }

    public int getCursorPosition() {
        return cursor.getPosition();
    }

    public int getBottomMidiVal() {
        return bottomMidiVal;
    }

    public int getTopMidiVal() {
        return topMidiVal;
    }
}
