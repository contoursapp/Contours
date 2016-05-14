package com.trcolgrove.contours.contoursGame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Object representing a single contour shape
 *
 * Contains a list of notes in the contour and some basic info about the shape.
 *
 * Created by Thomas on 6/25/15.
 */
public class Contour {

    private List<Note> notes;
    private int cursorPosition;
    private int topMidiVal;
    private int bottomMidiVal;
    private int Id;

    private String TAG = "Contour";

    private boolean isLaidOut = false;


    //TODO: compress constructors
    /**
     * Constructor for a contour shape
     *
     * @param id numerical value corresponding to the shape of the contour as specified in xml file
     * @param notes the notes contained in this contour shape
     */
    public Contour(int id, Note... notes) {
        Id = id;
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

        //store these values for easy access later
        this.topMidiVal = topNote;
        this.bottomMidiVal = bottomNote;
        this.notes = Arrays.asList(notes);
        cursorPosition = 0;
    }


    /**
     * Constructor for a contour shape
     *
     * @param id numerical value corresponding to the shape of the contour as specified in xml file
     * @param notes a list of notes contained in this contour shape
     */
    public Contour(int id, List<Note> notes) {
        Id = id;
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
        } else {
            cursorPosition = i;
        }
    }

    public Contour transposeBy(int amount) throws InvalidNoteException {
        List<Note> transposed = new ArrayList<>();
        for(Note n : notes) {
            transposed.add(n.transposeBy(amount));
        }
        return new Contour(Id, transposed);
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

    public boolean isLaidOut() {
        return isLaidOut;
    }

    public void setIsLaidOut(boolean isLaidOut) {
        this.isLaidOut = isLaidOut;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }
}
