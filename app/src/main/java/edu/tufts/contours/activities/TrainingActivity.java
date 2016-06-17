package edu.tufts.contours.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.gson.Gson;
import com.trcolgrove.contours.R;

import org.puredata.android.io.PdAudio;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import java.io.File;
import java.io.IOException;

import de.greenrobot.event.EventBus;
import edu.tufts.contours.colorfulPiano.Piano;
import edu.tufts.contours.contoursGame.ContoursGameView;
import edu.tufts.contours.contoursGame.ScoreBarItem;
import edu.tufts.contours.events.GameCompleteEvent;
import edu.tufts.contours.events.GameStartEvent;
import edu.tufts.contours.events.NoteEvent;
import edu.tufts.contours.events.ScoreEvent;
import edu.tufts.contours.synths.PdSynth;
import edu.tufts.contours.synths.Synth;
import edu.tufts.contours.synths.SynthInfo;
import jp.kshoji.driver.midi.activity.AbstractSingleMidiActivity;
import jp.kshoji.driver.midi.device.MidiInputDevice;
import jp.kshoji.driver.midi.device.MidiOutputDevice;


/**
 * The main training activity for the contours application.
 *
 * Initializes the Keyboard and the GameView, and handles Midi input
 * using Kshoji USB Midi Driver
 */
public class TrainingActivity extends AbstractSingleMidiActivity {

    private int[] multiplierBackgrounds = {R.drawable.multiplierbg_1, R.drawable.multiplierbg_2,
                                            R.drawable.multiplierbg_3, R.drawable.multiplierbg_4,
                                            R.drawable.multiplierbg_5, R.drawable.multiplierbg_6,
                                            R.drawable.multiplierbg_7, R.drawable.multiplierbg_8};

    //main views
    private Piano pianoView;
    private ContoursGameView gameView;
    private ProgressBar progressBar;

    // midi interface
    private MidiInputDevice midiIn;

    // synth stuff
    private SynthInfo synthInfo;
    private Synth synth;
    private String patchName;
    private String sound;

    // Text switchers and text factories for the score bar view
    private ScoreBarItem scoreText;
    private TextSwitcher progressText;
    private TextView scoreIncrementText;
    private TextSwitcher multiplierSwitcher;
    private Chronometer chronometer;
    private ScoreTextFactory scoreTextFactory = new ScoreTextFactory();
    private MultiplierTextFactory multiplierTextFactory = new MultiplierTextFactory();
    private ScoreBarFactory scoreBarFactory = new ScoreBarFactory();

    private int contoursCompleted = 1;

    // prevent tablet from going to sleep due to lack of touch input
    PowerManager.WakeLock cpuLock;

    // For debugging
    private String TAG = "TrainingActivity";

    // ACTIVITY LIFECYCLE FUNCTIONS

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        ScoreBarItem sb = (ScoreBarItem) findViewById(R.id.score_label);

        pianoView = (Piano) findViewById(R.id.piano);
        gameView = (ContoursGameView) findViewById(R.id.staff);
        progressBar = (ProgressBar) findViewById(R.id.training_loader);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        scoreText = (ScoreBarItem) sb.findViewById(R.id.score_label);
        multiplierSwitcher = ((ScoreBarItem) findViewById(R.id.multiplier)).getSwitcher();
        scoreIncrementText = (TextView) findViewById(R.id.score_increment);
        progressText = ((ScoreBarItem) findViewById(R.id.completed)).getSwitcher();
        progressText.setFactory(scoreBarFactory);
        progressText.setText(contoursCompleted + "/" + gameView.getContourCount());
        scoreText.setFactory(scoreTextFactory);
        scoreText.setText("0");
        multiplierSwitcher.setFactory(multiplierTextFactory);
        multiplierSwitcher.setCurrentText("x1");

        setMultiplierBackground(multiplierBackgrounds[0]);

        String synthJson = getIntent().getStringExtra("synth");
        synthInfo = new Gson().fromJson(synthJson, SynthInfo.class);

        try {
            initPd();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStart() {
        super.onStart();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        PdAudio.startAudio(this);
        EventBus.getDefault().register(this);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        cpuLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wake lock");
        cpuLock.acquire();
    }

    @Override
    protected void onStop() {
        super.onStop();
        PdAudio.stopAudio();
        EventBus.getDefault().unregister(this);
        cpuLock.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(midiIn != null) {
            midiIn.suspend();
            midiIn = null;
        }
        cleanup();
    }

    //EVENT HANDLERS
    public void onEvent(final NoteEvent event) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                if (event.velocity == 0) {
                    synth.noteOff(event.midiNote);
                } else {
                    synth.noteOn(event.midiNote, event.velocity);
                    gameView.processMidiInput(event.midiNote);
                }
            }
        });
    }

    public void onEvent(ScoreEvent event) {
        multiplierSwitcher.setText("x" + Integer.toString(event.multiplier));
        setMultiplierBackground(multiplierBackgrounds[event.multiplier - 1]);

        if(event.contourComplete) {
            contoursCompleted++;
            progressText.setText(contoursCompleted + "/" + gameView.getContourCount());
        }

        if(event.scoreIncrement != 0) {
            scoreText.setText(Integer.toString(event.totalScore));
            displayScoreIncrement(event.scoreIncrement);
        }
    }

    public void onEvent(GameCompleteEvent gce) {
        Intent i = new Intent(getApplicationContext(), EndReportActivity.class);
        gce.scoreBundle.putString("sound", synthInfo.soundName);
        i.putExtras(gce.scoreBundle);
        startActivity(i);
        this.finish();
    }

    // Animate Score Increment
    private void displayScoreIncrement(int scoreIncrement) {
        scoreIncrementText.setAlpha(1);
        if(scoreIncrement > 0) {
            scoreIncrementText.setText("+" + scoreIncrement);
            scoreIncrementText.setTextColor(Color.WHITE);
        } else {
            scoreIncrementText.setText(Integer.toString(scoreIncrement));
            scoreIncrementText.setTextColor(Color.RED);
        }
        scoreIncrementText.animate().alpha(1).setDuration(500).start();
        scoreIncrementText.animate().alpha(0).setStartDelay(500).setDuration(500).start();
    }

    private void cleanup() {
        PdAudio.release();
        PdBase.release();
    }

    private class resourcesLoader extends AsyncTask<File, Void, Void> {
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(File... dirs) {

            for(File dir : dirs) {
                String patchDir = "synthpatch";
                if(new File(dir.getAbsolutePath() + "/" + patchDir).exists()) {
                    continue;
                }
                try {
                    IoUtils.extractZipResource(getResources().openRawResource(R.raw.synthpatch),
                            dir, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        protected void onPostExecute(Void v) {
            PdSynth pdSynth = (PdSynth) synth;
            pdSynth.init(synthInfo);
            progressBar.setVisibility(View.GONE);
            chronometer.start();

            EventBus.getDefault().post(new GameStartEvent(SystemClock.uptimeMillis()));
        }
    }

    public void setPiano(View view) {
        PdBase.sendList("soundfont", "set", "piano_1");
    }

    private void initPd() throws IOException {
        synth = new PdSynth("synthpatch/" + synthInfo.patchFileName, this);
        new resourcesLoader().execute(this.getFilesDir());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_training, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setMultiplierBackground(int backgroundId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            multiplierSwitcher.setBackground(getResources().getDrawable(backgroundId, getTheme()));
        } else if(Build.VERSION.SDK_INT >= 16){
            multiplierSwitcher.setBackground(getResources().getDrawable(backgroundId));
        } else {
            multiplierSwitcher.setBackgroundDrawable(getResources().getDrawable(backgroundId));
        }
    }

    private class ScoreBarFactory implements ViewSwitcher.ViewFactory {
        @Override
        public View makeView() {
            // Create a new TextView
            TextView t = new TextView(TrainingActivity.this);
            t.setGravity(Gravity.TOP);
            t.setTextAppearance(TrainingActivity.this, android.R.style.TextAppearance_Large);
            t.setTextColor(Color.WHITE);
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
            t.setIncludeFontPadding(false);
            return t;
        }
    }

    private class ScoreTextFactory extends ScoreBarFactory {
        @Override
        public View makeView() {
            TextView t = (TextView) super.makeView();
            t.setMaxEms(4);
            t.setMinEms(4);
            t.setGravity(Gravity.TOP | Gravity.START);
            return t;
        }
    }

    private class MultiplierTextFactory extends ScoreBarFactory {
        @Override
        public View makeView() {
            TextView t = (TextView) super.makeView();
            t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            t.setMaxEms(2);
            return t;
        }
    }

    /**
     * MIDI LIBRARY FUNCTION DEFENITIONS
     *
     * Required to override all, but I only really use the first 4
     */

    @Override
    public void onMidiNoteOff(@NonNull final MidiInputDevice sender, int cable, int channel, int note, int velocity) {
        pianoView.noteOff(note, velocity);
    }

    @Override
    public void onMidiNoteOn(@NonNull final MidiInputDevice sender, int cable, int channel, int note, int velocity) {
        pianoView.noteOn(note, velocity);
    }

    @Override
    public void onMidiInputDeviceAttached(@NonNull MidiInputDevice midiInputDevice) {
        midiIn = midiInputDevice;
    }

    @Override
    public void onMidiOutputDeviceAttached(@NonNull final MidiOutputDevice midiOutputDevice) {
        Toast.makeText(this, "USB MIDI Device " + midiOutputDevice.getUsbDevice().getDeviceName() + " has been attached.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDeviceAttached(@NonNull UsbDevice usbDevice) {
        // deprecated method.
        // do nothing
    }

    @Override
    public void onDeviceDetached(@NonNull UsbDevice usbDevice) {
        // deprecated method.
        // do nothing
    }

    @Override
    public void onMidiInputDeviceDetached(@NonNull MidiInputDevice midiInputDevice) {

    }

    @Override
    public void onMidiOutputDeviceDetached(@NonNull final MidiOutputDevice midiOutputDevice) {
        Toast.makeText(this, "USB MIDI Device " + midiOutputDevice.getUsbDevice().getDeviceName() + " has been detached.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMidiPolyphonicAftertouch(@NonNull final MidiInputDevice sender, int cable, int channel, int note, int pressure) {
    }

    @Override
    public void onMidiControlChange(@NonNull final MidiInputDevice sender, int cable, int channel, int function, int value) {

    }

    @Override
    public void onMidiProgramChange(@NonNull final MidiInputDevice sender, int cable, int channel, int program) {

    }

    @Override
    public void onMidiChannelAftertouch(@NonNull final MidiInputDevice sender, int cable, int channel, int pressure) {
    }

    @Override
    public void onMidiPitchWheel(@NonNull final MidiInputDevice sender, int cable, int channel, int amount) {

    }

    @Override
    public void onMidiSystemExclusive(@NonNull final MidiInputDevice sender, int cable, final byte[] systemExclusive) {
    }

    @Override
    public void onMidiSystemCommonMessage(@NonNull final MidiInputDevice sender, int cable, final byte[] bytes) {
    }

    @Override
    public void onMidiSingleByte(@NonNull final MidiInputDevice sender, int cable, int byte1) {
    }

    @Override
    public void onMidiMiscellaneousFunctionCodes(@NonNull final MidiInputDevice sender, int cable, int byte1, int byte2, int byte3) {
    }

    @Override
    public void onMidiCableEvents(@NonNull final MidiInputDevice sender, int cable, int byte1, int byte2, int byte3) {
    }

}