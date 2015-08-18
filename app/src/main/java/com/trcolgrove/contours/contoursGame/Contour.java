package com.trcolgrove.contours.contoursGame;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Thomas on 6/25/15.
 */
public class Contour {

    private List<Note> notes;
    private int cursorPosition;
    private int topMidiVal;
    private int bottomMidiVal;
    private String TAG = "Contour";

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
        cursorPosition = 0;
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
    }

    public void setCursorPosition(int i) {
        if(cursorPosition >= notes.size()) {
            throw new IndexOutOfBoundsException(
                    "cursor position cannot be set to be greater than the size of the contour");
        }
        else {
            cursorPosition = i;
        }
    }

    public List<Note> getNotes() { return notes; }

    public void incrementCursorPosition() {
        cursorPosition++;
    }

    public int getCursorPosition() {
        return cursorPosition;
    }

    public int getBottomMidiVal() {
        return bottomMidiVal;
    }

    public int getTopMidiVal() {
        return topMidiVal;
    }
}
