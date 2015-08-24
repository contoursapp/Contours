package com.trcolgrove.contours.contoursGame;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.trcolgrove.contours.R;
import com.trcolgrove.daoentries.ScoreSet;
import com.trcolgrove.daoentries.SurveyResponse;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;

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
        if (isConnected()) {

            NameValuePair userId = new BasicNameValuePair("user_id", "android_man");
            NameValuePair totalScore = new BasicNameValuePair("total_score", Integer.toString(scoreSet.getTotal_score()));
            NameValuePair elapsedTime = new BasicNameValuePair("elapsed_time", Long.toString(scoreSet.getElapsed_time()));
            NameValuePair notesHit = new BasicNameValuePair("notes_hit", Integer.toString(scoreSet.getNotes_hit()));
            NameValuePair notesMissed = new BasicNameValuePair("notes_missed", Integer.toString(scoreSet.getNotes_missed()));
            NameValuePair longestStreak = new BasicNameValuePair("longest_streak", Integer.toString(scoreSet.getLongest_streak()));
            NameValuePair averageStreak = new BasicNameValuePair("average_streak", Integer.toString(scoreSet.getAverage_streak()));
            NameValuePair date = new BasicNameValuePair("date", scoreSet.getDate().toString());

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
                }
            });
            request.execute(userId, totalScore, elapsedTime, notesHit, notesMissed, longestStreak,
                    averageStreak, date);
        } else {
            scoreSet.setUploaded(false);
            dm.scoreSetDao.insertOrReplace(scoreSet);
        }
    }

    /**
     * Post a users "survey response" to the server
     *
     * @param surveyResponse
     */
    public void postSurveyResponse(final SurveyResponse surveyResponse) {
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
                }
            });
            request.execute(userId, question, response, date);
        } else {
            surveyResponse.setUploaded(false);
            dm.surveyResponseDao.insertOrReplace(surveyResponse);
        }
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
}


