package com.trcolgrove.contours.contoursGame;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.trcolgrove.daoentries.DaoMaster;
import com.trcolgrove.daoentries.DaoSession;
import com.trcolgrove.daoentries.ScoreSet;
import com.trcolgrove.daoentries.ScoreSetDao;
import com.trcolgrove.daoentries.SurveyResponse;
import com.trcolgrove.daoentries.SurveyResponseDao;

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
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "scores-db", null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        scoreSetDao = daoSession.getScoreSetDao();
        surveyResponseDao = daoSession.getSurveyResponseDao();
    }


    public void storeScoreSet(ScoreSet sc) {
        scoreSetDao.insert(sc);
    }

    public void storeSurveyResponse(SurveyResponse sr) {
        surveyResponseDao.insert(sr);
    }
}
