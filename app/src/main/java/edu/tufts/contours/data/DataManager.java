package edu.tufts.contours.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.trcolgrove.daoentries.DaoMaster;
import com.trcolgrove.daoentries.DaoSession;
import com.trcolgrove.daoentries.StoredSet;
import com.trcolgrove.daoentries.StoredSetDao;

/**
 * Singleton class implementing simple localized data storage
 * through greenDao implementation.
 *
 * Handles set up of orm and basic storage needs
 * Access is provided to scoreSetDao and surveyResponse for
 * making queries
 */
public class DataManager {

    private static final String TAG = "DataManager";

    private Context context;
    public StoredSetDao storedSetDao;

    public static final String PREFS_NAME = "PrefsFile";

    public DataManager(Context context) {
        this.context = context;
    }

    public void storeStoredSet(StoredSet s) {
        new AsyncTask<StoredSet, Void, Void> (){
            @Override
            protected Void doInBackground(StoredSet... params) {
                DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "scores-db", null);
                SQLiteDatabase db = helper.getWritableDatabase();
                DaoMaster daoMaster = new DaoMaster(db);
                DaoSession daoSession = daoMaster.newSession();
                StoredSetDao storedSetDao = daoSession.getStoredSetDao();
                try {
                    storedSetDao.insertOrReplace(params[0]);
                } finally {
                    daoMaster.getDatabase().close();
                    daoSession.getDatabase().close();
                    db.close();
                    helper.close();
                    daoSession.clear();
                    db=null;
                    daoSession=null;
                }
                return null;
            }
        }.execute(s);
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

}
