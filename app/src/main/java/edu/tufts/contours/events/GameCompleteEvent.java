package edu.tufts.contours.events;

import android.os.Bundle;

/**
 * Created by Thomas on 8/18/15.
 */
public class GameCompleteEvent {
    public Bundle scoreBundle;

    public GameCompleteEvent(Bundle scoreBundle) {
        this.scoreBundle = scoreBundle;
    }
}
