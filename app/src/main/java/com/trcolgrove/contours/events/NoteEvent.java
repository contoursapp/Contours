package com.trcolgrove.contours.events;

/**
 * Created by Thomas on 6/27/15.
 */
public class NoteEvent {
    public int midiNote;
    public int velocity;

    public NoteEvent(int midiNote, int velocity) {
        this.midiNote = midiNote;
        this.velocity = velocity;
    }
}
