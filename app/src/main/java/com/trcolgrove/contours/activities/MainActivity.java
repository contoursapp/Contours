package com.trcolgrove.contours.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.trcolgrove.contours.R;
import com.trcolgrove.contours.contoursGame.DataManager;
import com.trcolgrove.contours.contoursGame.ServerUtil;
import com.trcolgrove.contours.contoursGame.TrainingActivity;
import com.trcolgrove.daoentries.ScoreSet;
import com.trcolgrove.daoentries.ScoreSetDao;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;


public class MainActivity extends ActionBarActivity {

    private LinearLayout difficultyMenu; // Select difficulty menu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        difficultyMenu = (LinearLayout) findViewById(R.id.difficulty_menu);
        uploadPendingData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void playButtonClicked(View view) {
        difficultyMenu.setAlpha(0);
        difficultyMenu.setVisibility(View.VISIBLE);
        difficultyMenu.animate().alpha(1);
    }

    /* Select Difficulty Buttons */

    public void easyButtonClicked(View view) {
        Intent i = new Intent(getApplicationContext(), TrainingActivity.class);
        startActivity(i);
    }

    public void mediumButtonClicked(View view) {
        Intent i = new Intent(getApplicationContext(), TrainingActivity.class);
        startActivity(i);
    }

    public void hardButtonClicked(View view) {
        Intent i = new Intent(getApplicationContext(), TrainingActivity.class);
        startActivity(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Attempt to upload ScoreSet and Survey which have not yet been uploaded to the server
     * If the tablet is connected to the internet this function will locate
     * unuploaded Data and attempt to upload it
     */
    public void uploadPendingData() {
        ServerUtil serverUtil = new ServerUtil(getApplicationContext());
        if(serverUtil.isConnected()) {
            DataManager dm = new DataManager(getApplicationContext());
            QueryBuilder qb = dm.scoreSetDao.queryBuilder().where(ScoreSetDao.Properties.Uploaded.eq(false));
            List<ScoreSet> pendingUpload = qb.list();
            for (ScoreSet sc : pendingUpload) {
                serverUtil.postScoreSet(sc);
            }
            qb = dm.surveyResponseDao.queryBuilder().where(ScoreSetDao.Properties.Uploaded.eq(false));
            pendingUpload = qb.list();
            for (ScoreSet sc : pendingUpload) {
                serverUtil.postScoreSet(sc);
            }
        }

    }
}