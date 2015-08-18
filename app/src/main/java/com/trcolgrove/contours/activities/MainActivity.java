package com.trcolgrove.contours.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.trcolgrove.contours.R;
import com.trcolgrove.contours.contoursGame.TrainingActivity;


public class MainActivity extends ActionBarActivity {

    private LinearLayout difficultyMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        difficultyMenu = (LinearLayout) findViewById(R.id.difficulty_menu);
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
}