package com.trcolgrove.contours.contoursGame;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.trcolgrove.daoentries.DaoMaster;
import com.trcolgrove.daoentries.DaoSession;
import com.trcolgrove.daoentries.ScoreSet;
import com.trcolgrove.daoentries.ScoreSetDao;
import com.trcolgrove.daoentries.SurveyResponse;
import com.trcolgrove.daoentries.SurveyResponseDao;

import java.util.Date;

/**
 * Singleton class implementing simple localized data storage
 * through greenDao implementation.
 *
 * Handles set up of orm and basic storage needs
 * Access is provided to scoreSetDao and surveyRespose for
 * making queries
 */
public class DataManager {

    private SQLiteDatabase db;

    private DaoMaster daoMaster;
    private DaoSession daoSession;

    public ScoreSetDao scoreSetDao;
    public SurveyResponseDao surveyResponseDao;

    public DataManager(Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "notes-db", null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        scoreSetDao = daoSession.getScoreSetDao();
        surveyResponseDao = daoSession.getSurveyResponseDao();
    }

    public void storeScoreSet(String difficulty, int totalScore, Long elapsedTime,
                              Integer longestStreak, Integer averageStreak, Integer notesHit,
                              Integer notesMissed){
        storeScoreSet(difficulty, totalScore, elapsedTime, longestStreak, averageStreak, notesHit, notesMissed, new Date());
    }

    public void storeScoreSet(String difficulty, int totalScore, Long elapsedTime,
                              Integer longestStreak, Integer averageStreak, Integer notesHit,
                              Integer notesMissed, Date date) {
        ScoreSet sc = new ScoreSet(null, difficulty, totalScore, elapsedTime,
                notesHit, notesMissed, longestStreak, null, date);
        scoreSetDao.insert(sc);
    }

    public void storeSurveyResponse(String question, int response, Date date) {
        SurveyResponse sr = new SurveyResponse(null, question, response, date);
        surveyResponseDao.insert(sr);
    }
}
