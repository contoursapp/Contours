package edu.tufts.contours.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.trcolgrove.contours.R;
import edu.tufts.contours.data.DataManager;

public class LoginActivity extends ActionBarActivity {

    private String TAG = "LoginActivity";

    private EditText aliasText;
    private Button doneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        doneButton = (Button) findViewById(R.id.done_button);
        doneButton.setEnabled(false);

        aliasText = (EditText) findViewById(R.id.alias_select);
        aliasText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                    aliasInput();
                    return true;
                }
                return false;
            }
        });
        aliasText.requestFocus();
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void aliasInput() {
        aliasText.clearFocus();
        hideKeyboard();
        doneButton.setAlpha(0);
        doneButton.setVisibility(View.VISIBLE);
        doneButton.setEnabled(true);
        doneButton.animate().alpha(1);
    }


    public void doneButtonClicked(View view) {
        DataManager dm = new DataManager(getApplicationContext());
        dm.setUserAlias(aliasText.getText().toString());

        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
}
