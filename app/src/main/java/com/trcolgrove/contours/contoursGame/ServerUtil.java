package com.trcolgrove.contours.contoursGame;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.trcolgrove.daoentries.DaoMaster;
import com.trcolgrove.daoentries.DaoSession;
import com.trcolgrove.daoentries.StoredSet;
import com.trcolgrove.daoentries.StoredSetDao;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Semaphore;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Convenience class for interfacing with
 * and posting data to the contours server.
 *
 * Created by Thomas on 8/19/15.
 */
public class ServerUtil {

    protected DataManager dm;
    protected String servAddr;
    protected Context context;

    Cache cache;
    Network network;
    RequestQueue rq;

    //private RequestQueue rq = new RequestQueue();

    Gson gson = new Gson();
    private static String TAG = "ServerUtil";

    private final Semaphore available = new Semaphore(1);

    public ServerUtil(Context context) {
        dm = new DataManager(context);
        //servAddr = context.getResources().getString(R.string.serv_addr);
        servAddr = "http://trcolgrove.pagekite.me";
        this.context = context;
        cache = new DiskBasedCache(context.getCacheDir(), 1024 * 1024); // 1MB cap
        network = new BasicNetwork(new HurlStack());
        rq = new RequestQueue(cache, network);
        rq.start();
    }

    /**
     * Method encapsulating the procedure for storing a ScoreSet
     * A post request is generated based on the ScoreSet object
     * and sent to the server.
     *
     * <p>
     * If the server returns success, the uploaded field of the ScoreSet is set to true, and if it
     * returns an error, or the user is not connected to the internet, uploaded is set to false.
     * After, the ScoreSet is stored internally on the device.
     * </p>
     * @param scoreSet
     */
    public void postScoreSet(final ScoreSet scoreSet) {
        final String scoreJson = gson.toJson(scoreSet);
        final StoredSet store = new StoredSet();
        store.setScoreSetJson(scoreJson);
        postScoreSet(store);
    }

    public void postScoreSet(final StoredSet store) {
        JSONObject jsonBody = null;

        try {
            jsonBody = new JSONObject(store.getScoreSetJson());
            System.out.println(jsonBody.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, servAddr + "/addScoreSet",
                jsonBody, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject status) {
                store.setUploaded(true);
                dm.storeStoredSet(store);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError ve) {
                store.setUploaded(false);
                dm.storeStoredSet(store);
                ve.printStackTrace();
            }
        });
        rq.add(req);
    }

    public boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting());
    }

    public boolean postUser() {
        //TODO: implement
        return false;
    }

    /**
     * Attempt to upload ScoreSet and Survey which have not yet been uploaded to the server
     * If the tablet is connected to the internet this function will locate
     * unuploaded Data and attempt to upload it
     */
    public void uploadPendingData() {
        if (isConnected()) {
            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "scores-db", null);
            SQLiteDatabase db = helper.getWritableDatabase();
            DaoMaster daoMaster = new DaoMaster(db);
            DaoSession daoSession = daoMaster.newSession();
            StoredSetDao storedSetDao = daoSession.getStoredSetDao();

            QueryBuilder qb = storedSetDao.queryBuilder().where(StoredSetDao.Properties.Uploaded.eq(false));
            List<StoredSet> pendingUpload = qb.list();

            for (StoredSet s : pendingUpload) {
                postScoreSet(s);
            }
        }
    }
}


