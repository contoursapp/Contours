package edu.tufts.contours.synths;

import android.content.Context;
import android.support.annotation.StringDef;
import android.util.Log;

import org.puredata.core.PdBase;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Thomas on 10/1/15.
 */
public class SubtractiveSynth extends PdSynth {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            SINE,
            SQUARE,
            SAWTOOTH,
            TRIANGLE
    })
    public @interface WaveForm {}
    public static final String SINE = "sine_table";
    public static final String SQUARE = "square_table";
    public static final String SAWTOOTH = "sawtooth_table";
    public static final String TRIANGLE = "triangle_table";

    private static final int MAX_CHANNELS = 10;
    private static final String TAG = "SubtractiveSynth";

    HashMap<Integer, Integer> midiNoteToChannel = new HashMap<>();
    boolean channelAvailable[];

    public SubtractiveSynth(String pdPatchName, Context context) {
        super(pdPatchName, context);
        channelAvailable = new boolean[MAX_CHANNELS];
        Arrays.fill(channelAvailable, true);
    }

    @Override
    public void noteOn(int midiNum, int velocity) {
        int chan = getAvailableChannel();
        if(chan == -1) {
            Log.e(TAG , "exceeded max available channels: " + MAX_CHANNELS);
            return;
        }
        channelAvailable[chan] = false;
        midiNoteToChannel.put(midiNum, chan);
        PdBase.sendList("note", chan, midiNum, velocity);
    }


    @Override
    public void noteOff(int midiNum) {
        int chan = midiNoteToChannel.get(midiNum);
        PdBase.sendList("note", chan, midiNum, 0);
        midiNoteToChannel.remove(midiNum);
        channelAvailable[chan] = true;
    }

    /**
     * Set one of the 3 oscillators to a particular wave table
     * @param oscNum the oscillator to be set. Must be 1, 2 or 3
     * @param tableName the wave table to set for the oscillator. Ex. WaveForm.SQUARE
     */
    public void setOsc(int oscNum, @WaveForm String tableName) {
        if(oscNum > 3 || oscNum < 0) {
            throw new IllegalArgumentException("oscillator out of range. must be between 1 and 3");
        }
        PdBase.sendList("waveform", tableName);
    }

    private int getAvailableChannel() {
        for(int i = 0; i < channelAvailable.length; i++) {
            if(channelAvailable[i]) {
                return i;
            }
        }
        return -1; //no channels available
    }
}
