package edu.tufts.contours.events;

/**
 * Created by Thomas on 4/15/16.
 */
public class GameStartEvent {
    public long baseTime;

    public GameStartEvent(long baseTime) {
        this.baseTime = baseTime;
    }
}
