package com.trcolgrove.contours.contoursGame;

/**
 * Created by Thomas on 6/27/15.
 */
public class ContourCursor {

    private int position;

    public ContourCursor(int startPosition) {
        this.position = startPosition;

    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }



    public void incrementPosition() {
        this.position++;
    }


}
