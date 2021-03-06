package edu.tufts.contours.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.trcolgrove.contours.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.tufts.contours.data.DataManager;
import edu.tufts.contours.data.ServerUtil;
import edu.tufts.contours.synths.SynthInfo;
import edu.tufts.contours.util.FileIO;

/**
 * MainActivity
 *
 * The main activity for the contours app.
 * Displays a simple interface that presents the user with
 * the option to play the game at various difficulty settings
 */
public class MainActivity extends AppCompatActivity {

    private RelativeLayout difficultyMenu; // Select difficulty menu
    private LinearLayout infoText;
    private LinearLayout intervalMenu;
    private Intent trainingIntent;
    private int difficultyButton;
    RelativeLayout soundMenu;
    ListView soundList;

    private ArrayList<String> synthNames = new ArrayList<>();
    private Map<String, SynthInfo> synthInfoMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        difficultyMenu = (RelativeLayout) findViewById(R.id.difficulty_menu);
        infoText = (LinearLayout) findViewById(R.id.author_info);
        intervalMenu = (LinearLayout) findViewById(R.id.intervals);
        soundMenu = (RelativeLayout) findViewById(R.id.sound_menu);
        TextView aliasText = (TextView) findViewById(R.id.alias_text);
        soundList = (ListView)(findViewById(R.id.sound_list));

        ArrayList<SynthInfo> synths = FileIO.getAvailableSynths(this);

        for(SynthInfo s : synths) {
            synthNames.add(s.synthName);
            synthInfoMap.put(s.synthName, s);
        }

        soundList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.list_item, synthNames));

        soundList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SynthInfo si = synthInfoMap.get(synthNames.get(i));
                String synthJson = new Gson().toJson(si);
                trainingIntent.putExtra("synth", synthJson);
                startActivity(trainingIntent);
                finish();
            }
        });


        DataManager dm = new DataManager(getApplicationContext());
        String alias = dm.getUserAlias();

        trainingIntent = new Intent(getApplicationContext(), TrainingActivity.class);

        if(alias == null) {
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
            this.finish();
        }

        aliasText.setText("Alias: " + alias);
        ServerUtil serverUtil = new ServerUtil(getApplicationContext());
        serverUtil.uploadPendingData();

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
        infoText.setVisibility(View.GONE);
    }

    /* Select Difficulty Buttons */

    public void difficultyButtonClicked(View view) {
        String difficulty = ((Button)view).getText().toString();
        trainingIntent.putExtra("difficulty", difficulty);
        switch(difficulty) {
            case "easy":
                difficultyButton = R.id.easy_button;
                break;
            case "medium":
                difficultyButton = R.id.medium_button;
                break;
            case "hard":
                difficultyButton = R.id.hard_button;
                break;
        }
        showIntervalMenu();
    }

    private void showIntervalMenu() {
        intervalMenu.setAlpha(0);

        RelativeLayout.LayoutParams lp =
                (RelativeLayout.LayoutParams)intervalMenu.getLayoutParams();
        lp.addRule(RelativeLayout.ALIGN_BOTTOM, 0);
        lp.addRule(RelativeLayout.ALIGN_LEFT, 0);
        lp.addRule(RelativeLayout.ALIGN_RIGHT, 0);

        difficultyMenu.updateViewLayout(intervalMenu, lp);

        lp.addRule(RelativeLayout.ALIGN_LEFT, difficultyButton);
        lp.addRule(RelativeLayout.ALIGN_RIGHT, difficultyButton);

        intervalMenu.setVisibility(View.VISIBLE);
        intervalMenu.animate().alpha(1);
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

    public void intervalButtonClicked(View view) {
        int id = ((Button)view).getId();
        int intervalSize;
        switch(id) {
            case R.id.wideintervalbutton:
                intervalSize = 2;
                break;
            case R.id.mediumintervalbutton:
                intervalSize = 1;
                break;
            case R.id.smallintervalbutton:
                intervalSize = 0;
                break;
            default:
                intervalSize = 0;
        }
        trainingIntent.putExtra("interval_size", intervalSize);
        showSoundMenu();
    }


    public void showSoundMenu() {
        difficultyMenu.setVisibility(View.GONE);
        soundMenu.setAlpha(0);
        soundMenu.setVisibility(View.VISIBLE);
        soundMenu.animate().alpha(1);
    }
}
