package edu.tufts.contours.synths;

/**
 * Very basic interface for interacting with PureData synths
 * Created by Thomas on 9/29/15.
 */
public interface Synth {
    void noteOn(int midiNum, int velocity);
    void noteOff(int midiNum);
}
