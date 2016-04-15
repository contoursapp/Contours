package com.trcolgrove.contours.contoursGame;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to create contours from not objects, or from strings of
 * note names delimited by contours.
 *
 * Created by Thomas on 7/11/15.
 */
public class ContourFactory {
    private static final String TAG = "ContourFactory" ;

    /**
     * Given an array of Strings specifying notes, delimited by commas, return a list of
     * contour objects corresponding to the given note sequences.
     *
     * @param contourStrings An array of strings representing note contours eg. "A,B,C,F,G"
     * @param context The context of the activity where the contours are being created
     * @return a List of contour objects representing the given note sequences
     * @throws InvalidNoteException
     */
    public static List<Contour> getContoursFromStringArray(String[] contourStrings, Context context) throws InvalidNoteException {
        List<Contour> contours = new ArrayList<>();

        int id = 1; //TODO: make better system of assigning ids
        for(String contourString : contourStrings) {

            String[] noteStrings = contourString.split(",");
            List<Note> notes = new ArrayList<>();

            for(String noteString : noteStrings) {
                String letter = noteString.substring(0,1);
                int noteValue = 0;
                int octave = 0;
                try {
                    octave = Integer.parseInt(noteString.substring(1, 2));
                } catch(Exception e) {
                    Log.e(TAG, "Malformed Note! Please fix the contour specified in Arrays.xml");
                    continue;
                }

                switch(letter) {
                    case "C" :
                        notes.add(new Note(context, Note.C, octave));
                        break;
                    case "D" :
                        notes.add(new Note(context, Note.D, octave));
                        break;
                    case "E" :
                        notes.add(new Note(context, Note.E, octave));
                        break;
                    case "F" :
                        notes.add(new Note(context, Note.F, octave));
                        break;
                    case "G" :
                        notes.add(new Note(context, Note.G, octave));
                        break;
                    case "A" :
                        notes.add(new Note(context, Note.A, octave));
                        break;
                    case "B" :
                        notes.add(new Note(context, Note.B, octave));
                        break;
                    default:
                        Log.e(TAG, "Invalid note, " + letter + ", skipping instantiation");
                }
            }
            contours.add(new Contour(id, notes));
            id++;
        }
        return contours;
    }
}
