package edu.tufts.contours.synths;

/**
 * Created by Thomas on 6/15/16.
 */
public class SynthInfo {
    public String synthName;
    public String patchFileName;
    public String soundName;
    public Boolean isPolyphonic;
    public Integer numChannels;
    public Integer numOsc;
    public String[] oscSounds;
    public Float[] oscMix;
    public Integer[] oscOn;
    public Adsr adsr;
}
