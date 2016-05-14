package com.trcolgrove.contours;

import android.app.Activity;
import android.content.Intent;
import android.support.test.rule.ActivityTestRule;

/**
 * Created by Thomas on 4/12/16.
 */
public class TrainingActivityTestRule<T extends Activity> extends ActivityTestRule {
    public TrainingActivityTestRule(Class activityClass) {
        super(activityClass);
    }

    @Override
    protected Intent getActivityIntent () {
        Intent intent = new Intent();
        intent.putExtra("synth", "contours_patch.pd");
        intent.putExtra("sound", "sine_table");
        intent.putExtra("difficulty", "medium");
        intent.putExtra("interval_size", 2);
        intent.putExtra("test", true);

        return intent;
    }
}
