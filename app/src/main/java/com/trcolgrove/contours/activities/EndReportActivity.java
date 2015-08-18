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
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.trcolgrove.contours.R;


public class EndReportActivity extends ActionBarActivity {

    private RadioGroup surveyRg; //radio group for survey questions
    private TextSwitcher questionText;

    private String[] surveyQuestions; //An array of the questions being asked
    private int surveyIndex = 0;

    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_report);

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
        questionText.setText(surveyQuestions[surveyIndex++]);
        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        questionText.setInAnimation(in);
        questionText.setOutAnimation(out);

        surveyRg = (RadioGroup) findViewById(R.id.survey_rg);
        surveyRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(group.getCheckedRadioButtonId() != -1) {
                    nextButton.setEnabled(true);
                }
                //TODO: implement
            }
        });
        nextButton = (Button) findViewById(R.id.next_button);
        nextButton.setEnabled(false);

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


    private void displayNextSurveyQuestion() {
        questionText.setText(surveyQuestions[surveyIndex++]);
        nextButton.setEnabled(false);
        if(surveyIndex == surveyQuestions.length) {
            nextButton.setText("DONE>");
        }
    }

    public void onNextButtonClicked(View view) {
        if(surveyIndex < surveyQuestions.length) {
            displayNextSurveyQuestion();
            surveyRg.clearCheck();
            nextButton.setEnabled(false);
        } else {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        }
    }
}
