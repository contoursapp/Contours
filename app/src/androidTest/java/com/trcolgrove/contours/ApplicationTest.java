package com.trcolgrove.contours;

import com.trcolgrove.colorfulPiano.Piano;
import com.trcolgrove.contours.contoursGame.Contour;
import com.trcolgrove.contours.contoursGame.ContoursGameView;
import com.trcolgrove.contours.contoursGame.Note;
import com.trcolgrove.contours.contoursGame.TrainingActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(JUnit4.class)
public class ApplicationTest {

    private TrainingActivity mActivity;
    private Piano mPianoView;
    private List<Contour> mContours;

    @Rule
    public TrainingActivityTestRule<TrainingActivity> mActivityRule = new TrainingActivityTestRule<>(
            TrainingActivity.class);




    @Before
    public void setUp() throws Exception {
        mActivity = (TrainingActivity) mActivityRule.getActivity();
        mPianoView = (Piano) mActivity.findViewById(R.id.piano);
    }

    @Test
    public void basicTrainingTest() throws InterruptedException {
        mContours = ((ContoursGameView)mActivity.findViewById(R.id.staff)).getContours();

        Thread.sleep(2500);
        for(Contour c : mContours) {
            Thread.sleep(2500);
            for(Note n : c.getNotes()) {
                Thread.sleep(10);
                mPianoView.noteOn(n.getMidiValue(), 97);
                Thread.sleep(10);
                mPianoView.noteOff(n.getMidiValue(), 0);
            }
        }

    }
}