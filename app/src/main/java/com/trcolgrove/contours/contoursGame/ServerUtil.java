package com.trcolgrove.contours.contoursGame;

import android.content.Context;

import com.trcolgrove.contours.R;
import com.trcolgrove.daoentries.ScoreSet;
import com.trcolgrove.daoentries.SurveyResponse;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

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

    private static String TAG = "ServerUtil";

    public ServerUtil(Context context) {
        dm = new DataManager(context);
        servAddr = context.getResources().getString(R.string.serv_addr);
        scoreSetMethod = context.getResources().getString(R.string.add_score_set);
        surveyMethod = context.getResources().getString(R.string.add_survey_response);
    }


    /**
     * Post score set data to the server.
     *
     * @param scoreSet
     */
    public void postScoreSet(ScoreSet scoreSet) {
        NameValuePair userId = new BasicNameValuePair("user_id", "android_man");
        NameValuePair totalScore = new BasicNameValuePair("total_score", Integer.toString(scoreSet.getTotal_score()));
        NameValuePair elapsedTime = new BasicNameValuePair("elapsed_time", Long.toString(scoreSet.getElapsed_time()));
        NameValuePair notesHit = new BasicNameValuePair("notes_hit", Integer.toString(scoreSet.getNotes_hit()));
        NameValuePair notesMissed = new BasicNameValuePair("notes_missed", Integer.toString(scoreSet.getNotes_missed()));
        NameValuePair longestStreak = new BasicNameValuePair("longest_streak", Integer.toString(scoreSet.getLongest_streak()));
        NameValuePair averageStreak = new BasicNameValuePair("average_streak", Integer.toString(scoreSet.getAverage_streak()));
        NameValuePair date = new BasicNameValuePair("date", scoreSet.getDate().toString());

        new AsyncHttpRequest(servAddr + scoreSetMethod, AsyncHttpRequest.POST).execute(userId, totalScore, elapsedTime, notesHit, notesMissed, longestStreak,
                averageStreak, date);
    }

    /**
     * Post a users "survey response" to the server
     *
     * @param surveyResponse
     */
    public void postSurveyResponse(final SurveyResponse surveyResponse) {
        NameValuePair userId = new BasicNameValuePair("user_id", "android_man");
        NameValuePair question = new BasicNameValuePair("question", surveyResponse.getQuestion());
        NameValuePair response = new BasicNameValuePair("response", surveyResponse.getResponse().toString());
        NameValuePair date = new BasicNameValuePair("date", surveyResponse.getDate().toString());

        AsyncHttpRequest request = new AsyncHttpRequest(servAddr + surveyMethod, AsyncHttpRequest.POST);
        request.setOnComplete(new AsyncHttpRequest.HttpCallback() {
            @Override
            public void onFinished(int statusCode) {
                if(statusCode >= 200 && statusCode < 400) {
                    surveyResponse.setUploaded(true);
                    dm.surveyResponseDao.insertOrReplace(surveyResponse);
                } else {
                    surveyResponse.setUploaded(false);
                    dm.surveyResponseDao.insertOrReplace(surveyResponse);
                }
            }
        });
        request.execute(userId, question, response, date);
    }

    public boolean postUser() {
        //TODO: implement
        return false;
    }
}


