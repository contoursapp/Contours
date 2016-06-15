package edu.tufts.contours.contoursGame;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.trcolgrove.contours.R;

/**
 * Created by Thomas on 1/10/16.
 */
public class ScoreBarItem extends LinearLayout {

    private TextSwitcher textSwitcher;

    public ScoreBarItem(Context context) {
        this(context, null);
    }

    public ScoreBarItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScoreBarItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        ((Activity)getContext())
                .getLayoutInflater()
                .inflate(R.layout.score_bar_item, this, true);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ScoreBarItem, 0, 0);
            String header = a.getString(R.styleable.ScoreBarItem_header_text);
            TextView headerView = (TextView) findViewById(R.id.score_bar_header);
            headerView.setText(header);
            a.recycle();
        }

        textSwitcher = (TextSwitcher) findViewById(R.id.score_bar_value);

        Animation in = AnimationUtils.loadAnimation(context,
                android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(context,
                android.R.anim.fade_out);

        textSwitcher.setInAnimation(in);
        textSwitcher.setOutAnimation(out);

    }

    public void setFactory(ViewSwitcher.ViewFactory factory) {
        textSwitcher.setFactory(factory);
    }

    public TextSwitcher getSwitcher() {
        return (TextSwitcher) findViewById(R.id.score_bar_value);
    }
    public void setText(String text) {
        textSwitcher.setText(text);
    }
}
