package edu.tufts.contours.contoursGame;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import edu.tufts.contours.exceptions.InvalidNoteException;

/**
 * Utility class to transpose different shapes
 * Note that this class does not follow standard western
 * transposition, as our model only uses "the white keys"
 * on the piano.
 * Therefore transposing here will actually change the
 * melody of the shape played.
 * Created by Thomas on 11/15/15.
 */
public class Transposer {
    /* utility class to provide transposed versions of basic contour
    * shapes
    */
    public static List<Contour> transposeContours(Context context, Contour input)
            throws InvalidNoteException {

        List<Contour> transposedContours = new ArrayList<>();

        int i = 0;
        boolean inRange = true;
        while(inRange) {
            Contour transposed = input.transposeBy(i);
            if (transposed.getTopMidiVal() <= 84) {
                transposedContours.add(transposed);
            } else {
                inRange = false;
            }
            i++;
        }

        return transposedContours;
    }



}
