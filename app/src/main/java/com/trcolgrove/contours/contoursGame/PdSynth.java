package com.trcolgrove.contours.contoursGame;

import android.content.Context;
import android.util.Log;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.PdAudio;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.utils.PdDispatcher;

import java.io.File;
import java.io.IOException;

/**
 * Created by Thomas on 9/29/15.
 */
public abstract class PdSynth implements Synth {

    private String patchFilePath;
    private String TAG = "PdSynth";
    private static final int MIN_SAMPLE_RATE = 44100;

    /**
     * Construct an instance of a pure data synthesizer given the file name of the patch
     * and the context
     * @param pdPatchName the file name of the patch
     * @param context the application context in which the synth is being created
     */
    PdSynth(String pdPatchName, Context context) {
        AudioParameters.init(context);
        int srate = Math.max(MIN_SAMPLE_RATE, AudioParameters.suggestSampleRate());
        try {
            PdAudio.initAudio(srate, 0, 2, 1, true);
        } catch (IOException e) {
            Log.e(TAG, "failed to initialize audio");
            e.printStackTrace();

        }
        File dir = context.getFilesDir();
        File patchFile = new File(dir, pdPatchName);
        PdDispatcher dispatcher = new PdUiDispatcher();
        PdBase.setReceiver(dispatcher);
        patchFilePath = patchFile.getAbsolutePath();
    }

    /**
     * Initialize the synthesizer by opening the patch
     * Sould not be called until resources are loaded.
     */
    public void init() {
        try {
            PdBase.openPatch(patchFilePath);
        } catch (IOException e) {
            Log.e(TAG, "Failed to load synth resource");
        }
    }

    /**
     * Function representing the basic midiOn behavior for a pd synth module
     * @param midiNum The midi number for the pitch to be playerd
     * @param velocity the velocity of the note being played
     */
    public abstract void noteOn(int midiNum, int velocity);


    /**
     * Function representing the basic midiOff behavior for a pd synth module
     * @param midiNum The midi number for the pitch to be playerd
     */
    public abstract void noteOff(int midiNum);

}
