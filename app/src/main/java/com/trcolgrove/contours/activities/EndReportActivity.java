package com.trcolgrove.contours.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.trcolgrove.contours.R;
import com.trcolgrove.contours.contoursGame.DataManager;
import com.trcolgrove.contours.contoursGame.ServerUtil;
import com.trcolgrove.daoentries.ScoreSet;
import com.trcolgrove.daoentries.SurveyResponse;

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class EndReportActivity extends ActionBarActivity {

    private static final String TAG = "EndReportActivity";
    private RadioGroup surveyRg; //radio group for survey questions

    private TextSwitcher questionText; //text representing the current survey question

    private String[] surveyQuestions; //An array of the questions to be asked.

    private int surveyIndex = 0; // The index of the survey question being displayed

    private Button nextButton; // Element representing the nextButton in the survey panel

    private Date completionDate = new Date();

    private ServerUtil serverUtil;
    private DataManager dm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_report);

        serverUtil = new ServerUtil(getApplicationContext());
        dm = new DataManager(getApplicationContext());
        setScoreValues();

        surveyQuestions = getResources().getStringArray(R.array.survey_questions);

        questionText = (TextSwitcher) findViewById(R.id.question_text);
        questionText.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView myText = new TextView(EndReportActivity.this);
                myText.setTypeface(null, Typeface.BOLD);
                myText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
                myText.setTextColor(Color.WHITE);
                return myText;
            }
        });
        questionText.setText(surveyQuestions[surveyIndex]);
        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        questionText.setInAnimation(in);
        questionText.setOutAnimation(out);

        surveyRg = (RadioGroup) findViewById(R.id.survey_rg);
        surveyRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (group.getCheckedRadioButtonId() != -1) {
                    nextButton.setEnabled(true);
                }
                //TODO: implement
            }
        });
        nextButton = (Button) findViewById(R.id.next_button);
        nextButton.setEnabled(false);

        performIntroAnimations();
    }

    private void performIntroAnimations() {
        RelativeLayout totalScoreLayout = (RelativeLayout) findViewById(R.id.total_score_layout);
        RelativeLayout totalTimeLayout = (RelativeLayout) findViewById(R.id.total_time_layout);
        RelativeLayout notesHitLayout = (RelativeLayout) findViewById(R.id.notes_hit_layout);
        RelativeLayout accuracyLayout = (RelativeLayout) findViewById(R.id.accuracy_layout);
        TextView successText = (TextView) findViewById(R.id.success_text);

        int animDuration = 1000;
        int animDelayInterval = 500;
        int translationOffset = -1500;

        totalScoreLayout.setTranslationX(translationOffset);
        totalTimeLayout.setTranslationX(translationOffset);
        notesHitLayout.setTranslationX(translationOffset);
        accuracyLayout.setTranslationX(translationOffset);

        totalScoreLayout.animate().translationX(0).setDuration(animDuration).
                setStartDelay(animDelayInterval).setInterpolator(new OvershootInterpolator());
        totalTimeLayout.animate().translationX(0).setDuration(animDuration).
                setStartDelay(animDelayInterval*2).setInterpolator(new OvershootInterpolator());
        notesHitLayout.animate().translationX(0).setDuration(animDuration).
                setStartDelay(animDelayInterval*3).setInterpolator(new OvershootInterpolator());
        accuracyLayout.animate().translationX(0).setDuration(animDuration).
                setStartDelay(animDelayInterval * 4).setInterpolator(new OvershootInterpolator());
        successText.animate().alpha(1f).setDuration(2000);
    }

    /**
     * Private method to set the score values based the intent passed from the TrainingActivity
     * Should be called in onCreate()
     */
    private void setScoreValues() {
        String difficulty = getIntent().getStringExtra("difficulty");
        difficulty = "easy";

        int totalScore = getIntent().getIntExtra("total_score", -1);
        TextView totalScoreValue = (TextView) findViewById(R.id.total_score_value);
        totalScoreValue.setText(Integer.toString(totalScore));

        long totalTime = getIntent().getLongExtra("total_time", -1);
        TextView totalTimeValue = (TextView) findViewById(R.id.total_time_value);
        Date date = new Date(totalTime);
        DateFormat formatter = new SimpleDateFormat("mm:ss:SSS", Locale.ENGLISH);
        String dateFormatted = formatter.format(date);
        totalTimeValue.setText(dateFormatted);

        int notesHit = getIntent().getIntExtra("notes_hit", -1);
        TextView notesHitValue = (TextView) findViewById(R.id.notes_hit_value);
        notesHitValue.setText(Integer.toString(notesHit));

        int notesMissed = getIntent().getIntExtra("notes_missed", -1);
        float accuracy = (((float)notesHit)/(notesMissed+notesHit)) * 100;
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        TextView accuracyValue = (TextView) findViewById(R.id.accuracy_value);
        accuracyValue.setText(df.format(accuracy) + "%");

        int longestStreak = getIntent().getIntExtra("longest_streak", -1);

        int averageStreak = getIntent().getIntExtra("average_streak", -1);

        ScoreSet sc = new ScoreSet(null, dm.getUserAlias(), difficulty, totalScore, totalTime,
                notesHit, notesMissed, longestStreak, averageStreak, completionDate, false);
        serverUtil.postScoreSet(sc);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_end_report, menu);
        return true;
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
     * Display the next research survey question
     */
    private void displayNextSurveyQuestion() {
        surveyIndex++;
        questionText.setText(surveyQuestions[surveyIndex]);
        nextButton.setEnabled(false);
        if(surveyIndex == surveyQuestions.length - 1) {
            nextButton.setText("DONE>");
        }
    }

    /**
     * Click function for the survey's next button
     * @param view
     */
    public void onNextButtonClicked(View view) {
        int rbId = surveyRg.getCheckedRadioButtonId();
        RadioButton selected = (RadioButton) findViewById(rbId);
        int response = Integer.parseInt(selected.getText().toString());
        SurveyResponse sr = new SurveyResponse(null, surveyQuestions[surveyIndex], response, completionDate, false);
        serverUtil.postSurveyResponse(sr);

        if (surveyIndex < surveyQuestions.length - 1) {
            displayNextSurveyQuestion();
            surveyRg.clearCheck();
            nextButton.setEnabled(false);
        } else {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            this.finish();
        }
    }
}
