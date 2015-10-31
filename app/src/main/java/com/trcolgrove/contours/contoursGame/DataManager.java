package com.trcolgrove.contours.contoursGame;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.trcolgrove.daoentries.DaoMaster;
import com.trcolgrove.daoentries.DaoSession;
import com.trcolgrove.daoentries.ScoreSet;
import com.trcolgrove.daoentries.ScoreSetDao;
import com.trcolgrove.daoentries.SurveyResponse;
import com.trcolgrove.daoentries.SurveyResponseDao;

import java.util.concurrent.Semaphore;

/**
 * Singleton class implementing simple localized data storage
 * through greenDao implementation.
 *
 * Handles set up of orm and basic storage needs
 * Access is provided to scoreSetDao and surveyRespose for
 * making queries
 */
public class DataManager {

    private static final String TAG = "DataManager";
    private SQLiteDatabase db;

    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private DaoMaster.DevOpenHelper helper;
    private Context context;
    private Integer activeRequests;

    private final Semaphore available = new Semaphore(1);

    public ScoreSetDao scoreSetDao;
    public SurveyResponseDao surveyResponseDao;

    public static final String PREFS_NAME = "PrefsFile";

    public DataManager(Context context) {
        this.context = context;
        activeRequests = 0;
    }


    public void storeScoreSet(ScoreSet sc) {
        scoreSetDao.insert(sc);
    }

    public void storeSurveyResponse(SurveyResponse sr) {
        surveyResponseDao.insert(sr);
    }

    public void close() {
        daoMaster.getDatabase().close();
        daoSession.getDatabase().close();
        db.close();
        helper.close();
        daoSession.clear();
        db=null;
        //helper=null;
        daoSession=null;
    }

    public void open() {
        helper = new DaoMaster.DevOpenHelper(context, "scores-db", null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        scoreSetDao = daoSession.getScoreSetDao();
        surveyResponseDao = daoSession.getSurveyResponseDao();

    }

    public boolean isOpen() {
        return (db != null);
    }

    public String getUserAlias() {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);
        return preferences.getString("alias", null);
    }

    public void setUserAlias(String alias) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("alias", alias);
        editor.apply();
    }

    //Functions for concurrent access
    public void incrementActiveRequests() {
        activeRequests++;
    }
    public void decrementActiveRequests() {
        activeRequests--;
    }

    public int getActiveRequests() {
        return activeRequests;
    }
}
