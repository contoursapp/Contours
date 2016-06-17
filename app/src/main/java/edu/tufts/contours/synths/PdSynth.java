package edu.tufts.contours.synths;

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
public class PdSynth implements Synth {

    private String patchFilePath;
    private static final int MIN_SAMPLE_RATE = 44100;
    private static final String TAG = "PdSynth";

    /**
     * Construct an instance of a pure data synthesizer given the file name of the patch
     * and the context
     * @param pdPatchName the file name of the patch
     * @param context the application context in which the synth is being created
     */
    public PdSynth(String pdPatchName, Context context) {
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

    public void setMix(Float[] mix) {
        Object[] args = new Object[mix.length + 1];

        args[0] = "mix";
        for(int i = 1; i < args.length; i ++) {
            args[i] = mix[i-1];
        }

        PdBase.sendList("set_mix", args);
    }

    public void setSound(String soundName) {
        Log.d(TAG, soundName);
        PdBase.sendList("set_sound", "set", soundName);
    }

    public void setOscSound(String soundName, int osc) {
        PdBase.sendList("set_osc_sound", "osc" + 1, "set", soundName);
    }

    public void setOscOn(int on, int osc) {
        PdBase.sendList("set_osc_on", "osc" + osc, "on", on);
    }

    public void setAdsr(int attack, int decay, float sustain, int release) {
        PdBase.sendList("set_adsr", "adsr", attack, decay, sustain, release);
    }

    /**
     * Initialize the synthesizer by opening the patch
     * Sould not be called until resources are loaded.
     */
    public void init(SynthInfo synthInfo) {
        try {
            PdBase.openPatch(patchFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(synthInfo.soundName != null) {
            setSound(synthInfo.soundName);
        }
        if(synthInfo.oscSounds != null) {
            for(int i = 0; i < synthInfo.oscSounds.length; i++) {
                setOscSound(synthInfo.oscSounds[i], i + 1);
            }
        }
        if(synthInfo.oscMix != null) {
            setMix(synthInfo.oscMix);
        }
        if(synthInfo.oscOn != null) {
            for(int i = 0; i < synthInfo.oscOn.length; i++) {
               setOscOn(synthInfo.oscOn[i], i+1);
            }
        }
        if(synthInfo.adsr != null) {
            setAdsr(synthInfo.adsr.attack, synthInfo.adsr.decay,
                    synthInfo.adsr.sustain, synthInfo.adsr.release);
        }
    }

    /**
     * Function representing the basic midiOn behavior for a pd synth module
     * @param midiNum The midi number for the pitch to be playerd
     * @param velocity the velocity of the note being played
     */
    public void noteOn(int midiNum, int velocity) {
        PdBase.sendList("note", midiNum, velocity);
    }

    public void noteOff(int midiNum) { PdBase.sendList("note", midiNum, 0); }

}
