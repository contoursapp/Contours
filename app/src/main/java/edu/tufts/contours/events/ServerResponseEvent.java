package edu.tufts.contours.events;

/**
 * Created by Thomas on 6/17/16.
 */
public class ServerResponseEvent {
    public boolean success;

    public ServerResponseEvent(boolean success) {
        this.success = success;
    }
}
