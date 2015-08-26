package com.trcolgrove.contours.contoursGame;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.trcolgrove.contours.R;
import com.trcolgrove.daoentries.ScoreSet;
import com.trcolgrove.daoentries.ScoreSetDao;
import com.trcolgrove.daoentries.SurveyResponse;
import com.trcolgrove.daoentries.SurveyResponseDao;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
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
    protected String scoreSetMethod;
    protected String surveyMethod;
    protected Context context;

    private static String TAG = "ServerUtil";

    private final Semaphore available = new Semaphore(1);

    public ServerUtil(Context context) {
        dm = new DataManager(context);
        servAddr = context.getResources().getString(R.string.serv_addr);
        scoreSetMethod = context.getResources().getString(R.string.add_score_set);
        surveyMethod = context.getResources().getString(R.string.add_survey_response);
        this.context = context;
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

        openDataManagerIfClosed();

        NameValuePair userId, totalScore, elapsedTime, notesHit, notesMissed, longestStreak, averageStreak, date;
        if (isConnected()) {
            try {
                userId = new BasicNameValuePair("user_id", "android_man");
                totalScore = new BasicNameValuePair("total_score", Integer.toString(scoreSet.getTotal_score()));
                elapsedTime = new BasicNameValuePair("elapsed_time", Long.toString(scoreSet.getElapsed_time()));
                notesHit = new BasicNameValuePair("notes_hit", Integer.toString(scoreSet.getNotes_hit()));
                notesMissed = new BasicNameValuePair("notes_missed", Integer.toString(scoreSet.getNotes_missed()));
                longestStreak = new BasicNameValuePair("longest_streak", Integer.toString(scoreSet.getLongest_streak()));
                averageStreak = new BasicNameValuePair("average_streak", Integer.toString(scoreSet.getAverage_streak()));
                date = new BasicNameValuePair("date", scoreSet.getDate().toString());
            } catch (NullPointerException ne) {
                Log.e(TAG, "one of scoresets fields was null");
                return;
            }
            AsyncHttpRequest request = new AsyncHttpRequest(servAddr + File.separator + scoreSetMethod, AsyncHttpRequest.POST);
            request.setOnComplete(new AsyncHttpRequest.HttpCallback() {
                @Override
                public void onFinished(int statusCode) {
                    if (statusCode >= 200 && statusCode < 400) {
                        scoreSet.setUploaded(true);
                        dm.scoreSetDao.insertOrReplace(scoreSet);
                    } else {
                        scoreSet.setUploaded(false);
                        dm.scoreSetDao.insertOrReplace(scoreSet);
                    }
                    closeDataManagerIfFinished();
                }
            });
            request.execute(userId, totalScore, elapsedTime, notesHit, notesMissed, longestStreak,
                    averageStreak, date);
        } else {
            scoreSet.setUploaded(false);
            dm.scoreSetDao.insertOrReplace(scoreSet);
            closeDataManagerIfFinished();
        }
    }

    /**
     * Post a users "survey response" to the server
     *
     * @param surveyResponse
     */
    public void postSurveyResponse(final SurveyResponse surveyResponse) {

        openDataManagerIfClosed();

        if(isConnected()) {
            NameValuePair userId = new BasicNameValuePair("user_id", "android_man");
            NameValuePair question = new BasicNameValuePair("question", surveyResponse.getQuestion());
            NameValuePair response = new BasicNameValuePair("response", surveyResponse.getResponse().toString());
            NameValuePair date = new BasicNameValuePair("date", surveyResponse.getDate().toString());

            AsyncHttpRequest request = new AsyncHttpRequest(servAddr + "/" + surveyMethod, AsyncHttpRequest.POST);
            request.setOnComplete(new AsyncHttpRequest.HttpCallback() {
                @Override
                public void onFinished(int statusCode) {
                    if (statusCode >= 200 && statusCode < 400) {
                        surveyResponse.setUploaded(true);
                        dm.surveyResponseDao.insertOrReplace(surveyResponse);
                    } else {
                        surveyResponse.setUploaded(false);
                        dm.surveyResponseDao.insertOrReplace(surveyResponse);
                    }
                    closeDataManagerIfFinished();
                }
            });
            request.execute(userId, question, response, date);
        } else {
            surveyResponse.setUploaded(false);
            dm.surveyResponseDao.insertOrReplace(surveyResponse);
            closeDataManagerIfFinished();
        }
    }

    public boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting());
    }

    private void openDataManagerIfClosed() {
        try {
            available.acquire();
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted!");
        }
        if(!dm.isOpen()) {
            dm.open();
        }
        dm.incrementActiveRequests();
        available.release();
    }

    private void closeDataManagerIfFinished() {
        try {
            available.acquire();
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted!");
        }
        dm.decrementActiveRequests();
        if(dm.getActiveRequests() == 0) {
            dm.close();
        }
        available.release();
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
        if(isConnected()) {
            openDataManagerIfClosed();
            QueryBuilder qb = dm.scoreSetDao.queryBuilder().where(ScoreSetDao.Properties.Uploaded.eq(false));
            List<ScoreSet> scPendingUpload = qb.list();
            qb = dm.surveyResponseDao.queryBuilder().where(SurveyResponseDao.Properties.Uploaded.eq(false));
            List<SurveyResponse> srPendingUpload = qb.list();
            closeDataManagerIfFinished();

            for (ScoreSet sc : scPendingUpload) {
                postScoreSet(sc);
            }

            for (SurveyResponse sr : srPendingUpload) {
                postSurveyResponse(sr);
            }
        }
    }
}


