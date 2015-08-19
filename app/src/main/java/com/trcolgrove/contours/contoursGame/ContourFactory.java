package com.trcolgrove.contours.contoursGame;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 7/11/15.
 */
public class ContourFactory {
    private static final String TAG = "ContourFactory" ;

    //TODO: implement this class, allow for easy definition of contours

    public static Contour createContour(String noteNames) {
        String[] noteStrings = noteNames.split(",");
        for(String noteString : noteStrings) {
        }
        return null;
    }

    public static Contour createContour(Note... notes) {
        return null;
    }

    public static List<Contour> getContoursFromStringArray(String[] contourStrings, Context context) throws InvalidNoteException {
        List<Contour> contours = new ArrayList<>();
        for(String contourString : contourStrings) {
            String[] noteStrings = contourString.split(",");
            List<Note> notes = new ArrayList<>();
            for(String noteString : noteStrings) {
                String letter = noteString.substring(0,1);
                int noteValue = 0;
                int octave = Integer.parseInt(noteString.substring(1, 2));

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
            contours.add(new Contour(notes));
        }
        return contours;
    }
}
