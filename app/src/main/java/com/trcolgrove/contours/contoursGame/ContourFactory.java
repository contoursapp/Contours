package com.trcolgrove.contours.contoursGame;

/**
 * Created by Thomas on 7/11/15.
 */
public class ContourFactory {

    //TODO: implement this class, allow for easy definition of contours

    public ContourFactory() {

    }

    public Contour createContour(String noteNames) {
        String[] noteStrings = noteNames.split(",");
        for(String noteString : noteStrings) {

        }
        return null;
    }

    public Contour createContour(Note... notes) {
        return null;
    }
}
