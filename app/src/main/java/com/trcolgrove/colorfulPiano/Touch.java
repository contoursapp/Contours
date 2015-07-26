package com.trcolgrove.colorfulPiano;

import java.util.ArrayList;

public class Touch {
    private ArrayList<PianoKey> keys = new ArrayList<PianoKey>();
    
    public Boolean isPressing(PianoKey key){
        return this.keys.contains(key);
    }

    public void press(PianoKey key, int midiVal) {
        if(this.isPressing(key)){
            return;
        }
        key.press(this, midiVal);
        this.keys.add(key);
    }
    
    public void lift(int bottomNote){
        for(PianoKey key : keys){
            int midiVal = key.getNoteValue() + bottomNote;
            key.unpress(this, midiVal);
        }
        keys.clear();
    }
}
