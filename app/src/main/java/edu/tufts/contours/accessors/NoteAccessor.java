package edu.tufts.contours.accessors;

import edu.tufts.contours.contoursGame.Note;

import aurelienribon.tweenengine.TweenAccessor;

/**
 * Created by Thomas on 7/30/15.
 *
 * For use with the Java Universal Tween Engine
 * this is a good library because it bypasses some
 * of the backend optimizations that seem to make
 * the ObjectAnimator class run slow on non-touch
 * events
 */
public class NoteAccessor implements TweenAccessor<Note> {

    // The following lines define the different possible tween types.
    // It's up to you to define what you need :-)

    public static final int RIPPLE_ALPHA = 1;
    public static final int RIPPLE_RADIUS = 2;

    // TweenAccessor implementation

    @Override
    public int getValues(Note target, int tweenType, float[] returnValues) {
        switch(tweenType) {
            case RIPPLE_ALPHA:
                returnValues[0] = target.getRippleAlpha();
                return 1;
            case RIPPLE_RADIUS:
                returnValues[0] = target.getRippleRadius();
                return 2;
            default: assert false; return -1;
        }

    }

    @Override
    public void setValues(Note target, int tweenType, float[] newValues) {
        switch(tweenType) {
            case RIPPLE_ALPHA:
                target.setRippleAlpha((int)newValues[0]);
                return;
            case RIPPLE_RADIUS:
                target.setRippleRadius((int)newValues[0]);
        }

    }
}