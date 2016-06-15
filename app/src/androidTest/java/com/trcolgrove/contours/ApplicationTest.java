package com.trcolgrove.contours;

import android.os.Bundle;
import android.os.SystemClock;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.tufts.contours.colorfulPiano.Piano;
import edu.tufts.contours.contoursGame.Contour;
import edu.tufts.contours.contoursGame.ContoursGameView;
import edu.tufts.contours.contoursGame.ContoursScoreKeeper;
import edu.tufts.contours.contoursGame.Note;
import edu.tufts.contours.contoursGame.ScoreSingle;
import edu.tufts.contours.activities.TrainingActivity;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(JUnit4.class)
public class ApplicationTest {

    private TrainingActivity mActivity;
    private Piano mPianoView;
    private ContoursScoreKeeper mScoreKeeper;
    private ContoursGameView gameView;
    private static final double EPSILON = 1e-3;

    @Rule
    public TrainingActivityTestRule<TrainingActivity> mActivityRule = new TrainingActivityTestRule<>(
            TrainingActivity.class);

    @Before
    public void setUp() throws Exception {
        mActivity = (TrainingActivity) mActivityRule.getActivity();
        mPianoView = (Piano) mActivity.findViewById(R.id.piano);
        gameView = ((ContoursGameView)mActivity.findViewById(R.id.staff));
        mScoreKeeper = gameView.getScoreKeeper();
    }

    public void assertEqualWithError(long expected, long actual, long error) {
        Assert.assertTrue("expected: " + expected + " actual: " + actual , actual <= expected + error && actual >= expected - error);
    }

    @Test
    public void trainingTestNoErrors() throws InterruptedException {
        List<Contour> mContours = gameView.getContours();

        int waitMilis = 200;
        int holdMilis = 100;
        int contourChangeMilis = ContoursGameView.getTransitionMilis();
        int contourSize = mContours.get(0).getNotes().size();

        gameView.startLock.await();
        long startTime = SystemClock.uptimeMillis();
        for(Contour c : mContours) {
            for(Note n : c.getNotes()) {
                Thread.sleep(waitMilis);
                mPianoView.noteOn(n.getMidiValue(), 97);
                Thread.sleep(holdMilis);
                mPianoView.noteOff(n.getMidiValue(), 0);
            }
            Thread.sleep(contourChangeMilis);
        }

        Bundle scoreBundle = mScoreKeeper.getScoreBundle();

        long timeOffset = startTime - mScoreKeeper.getBaseTime();

        long expectedDurationMilis = gameView.getContourCount()
                * ((contourSize *(waitMilis + holdMilis)) + contourChangeMilis) + timeOffset;
        String expectedSound = "sine_table";
        String expectedDifficulty = "medium";
        int expectedIntervalSize = 2;
        int expectedLongestStreak = contourSize * mContours.size();
        int expectedNoteHit =  contourSize * mContours.size();
        int expectedNoteMiss = 0;
        int expectedAverageStreak = expectedLongestStreak;

        assertEqualWithError(expectedDurationMilis, scoreBundle.getLong("total_time"), 300);
        assertEquals(expectedLongestStreak, scoreBundle.getInt("longest_streak"));
        assertEquals(expectedNoteHit, scoreBundle.getInt("notes_hit"));
        assertEquals(expectedNoteMiss, scoreBundle.getInt("notes_missed"));
        assertEquals(expectedAverageStreak, scoreBundle.getInt("average_streak"));
        assertEquals(expectedIntervalSize, scoreBundle.getInt("interval_size"));
        assertEquals(expectedSound, scoreBundle.getString("sound"));
        assertEquals(expectedDifficulty, scoreBundle.getString("difficulty"));

        long expectedPerContourDuration = (contourSize *(waitMilis + holdMilis));
        long expectedSuccessDuration = expectedPerContourDuration;
        double expectedPercentError = 0.0;
        double expectedInterOnsetStdDev = 0.0;

        String singles = scoreBundle.getString("indiv_contour_info");
        Gson gson = new Gson();
        ArrayList<ScoreSingle> scoreSingles = new Gson().fromJson(singles,
                new TypeToken<ArrayList<ScoreSingle>>() {
                }.getType());

        Assert.assertTrue(scoreSingles != null);

        for(int i = 0; i < scoreSingles.size(); i++) {

            long expectedDur = i == 0 ? expectedPerContourDuration + timeOffset : expectedPerContourDuration;
            double expectedStdDev = i == 0 ?
                    Math.sqrt(((double)(timeOffset * timeOffset) / (contourSize - 1))) : expectedInterOnsetStdDev;

            assertEqualWithError(expectedDur, scoreSingles.get(i).getCompletionTime(), 120);
            assertEqualWithError(expectedDur, scoreSingles.get(i).getSuccessDuration(), 120);
            assertEquals(expectedPercentError, scoreSingles.get(i).getPercentError(), EPSILON);
            assertEquals(expectedDifficulty, scoreSingles.get(i).getDifficulty());
            assertEquals(expectedIntervalSize, scoreSingles.get(i).getNoteGap());
            assertEquals(expectedSound, scoreSingles.get(i).getSound());
            assertEquals(0, scoreSingles.get(i).getNumberOfErrors());
            assertEquals(expectedStdDev, scoreSingles.get(i).getInterOnsetIntervalStdDev(), 120.0);

        }
    }

    @Test
    public void trainingTestOneErrorPerContour() throws InterruptedException {
        List<Contour> mContours = gameView.getContours();

        int waitMilis = 300;
        int holdMilis = 200;
        int contourChangeMilis = ContoursGameView.getTransitionMilis();
        int contourFailureMilis = ContoursGameView.getFailureTransitionMilis();
        int contourSize = mContours.get(0).getNotes().size();

        ArrayList<Long> expectedContourTimes;

        int expectedNoteHit = 0;
        int expectedNoteMiss = 0;

        gameView.startLock.await();
        long startTime = SystemClock.uptimeMillis();
        for(int i = 0; i < mContours.size(); i++) {
            int j;
            for(j = 0; j < i%contourSize; j++) {
                Note n = mContours.get(i).getNotes().get(j);
                Thread.sleep(waitMilis);
                mPianoView.noteOn(n.getMidiValue(), 97);
                expectedNoteHit++;
                Thread.sleep(holdMilis);
                mPianoView.noteOff(n.getMidiValue(), 0);
            }
            Note note = mContours.get(i).getNotes().get(j);
            Thread.sleep(waitMilis);
            mPianoView.noteOn(note.getMidiValue() + 1, 97);
            Thread.sleep(holdMilis);
            mPianoView.noteOff(note.getMidiValue() + 1, 0);
            expectedNoteMiss ++;
            Thread.sleep(contourFailureMilis);

            for(Note n : mContours.get(i).getNotes()) {
                Thread.sleep(waitMilis);
                mPianoView.noteOn(n.getMidiValue(), 97);
                expectedNoteHit++;
                Thread.sleep(holdMilis);
                mPianoView.noteOff(n.getMidiValue(), 0);
            }
            Thread.sleep(contourChangeMilis);
        }

        Bundle scoreBundle = mScoreKeeper.getScoreBundle();

        long timeOffset = startTime - mScoreKeeper.getBaseTime();

        String expectedSound = "sine_table";
        String expectedDifficulty = "medium";
        int expectedIntervalSize = 2;
        int expectedLongestStreak = contourSize * 2 - 1;
        int expectedAverageStreak = expectedNoteHit / (mContours.size() + 1);

        long expectedDurationMilis = ((expectedNoteHit + expectedNoteMiss)
                * (waitMilis + holdMilis)) + (contourChangeMilis * mContours.size())
                + (contourFailureMilis * mContours.size()) + timeOffset;

        assertEqualWithError(expectedDurationMilis, scoreBundle.getLong("total_time"), 300);
        assertEquals(expectedLongestStreak, scoreBundle.getInt("longest_streak"));
        assertEquals(expectedNoteHit, scoreBundle.getInt("notes_hit"));
        assertEquals(expectedNoteMiss, scoreBundle.getInt("notes_missed"));
        assertEquals(expectedAverageStreak, scoreBundle.getInt("average_streak"));
        assertEquals(expectedIntervalSize, scoreBundle.getInt("interval_size"));
        assertEquals(expectedSound, scoreBundle.getString("sound"));
        assertEquals(expectedDifficulty, scoreBundle.getString("difficulty"));

        String singles = scoreBundle.getString("indiv_contour_info");
        Gson gson = new Gson();
        ArrayList<ScoreSingle> scoreSingles = new Gson().fromJson(singles,
                new TypeToken<ArrayList<ScoreSingle>>() {
                }.getType());

        Assert.assertTrue(scoreSingles != null);

        for(int i = 0; i < scoreSingles.size(); i++) {
            int expectedNotesHit = i%contourSize + contourSize;
            int expectedNumErrors = 1;
            int numNotes = expectedNotesHit + expectedNumErrors;
            long expectedPerContourDuration = numNotes * (holdMilis + waitMilis);
            long expectedSuccessDuration = contourSize * (holdMilis + waitMilis);
            double expectedPercentError = (double)expectedNumErrors/(double)numNotes * 100;
            double expectedInterOnsetStdDev = 0.0;

            long expectedDur = i == 0 ? expectedPerContourDuration + timeOffset : expectedPerContourDuration;

            double expectedStdDev = i == 0 ?
                    Math.sqrt(((double)(timeOffset * timeOffset) / (contourSize - 1))) : expectedInterOnsetStdDev;

            assertEqualWithError(expectedDur, scoreSingles.get(i).getCompletionTime(), 200);
            assertEqualWithError(expectedSuccessDuration, scoreSingles.get(i).getSuccessDuration(), 200);
            assertEquals(expectedPercentError, scoreSingles.get(i).getPercentError(), EPSILON);
            assertEquals(expectedDifficulty, scoreSingles.get(i).getDifficulty());
            assertEquals(expectedIntervalSize, scoreSingles.get(i).getNoteGap());
            assertEquals(expectedSound, scoreSingles.get(i).getSound());
            assertEquals(expectedNumErrors, scoreSingles.get(i).getNumberOfErrors());
//            assertEquals(expectedStdDev, scoreSingles.get(i).getInterOnsetIntervalStdDev(), 200.0);
        }
    }
}