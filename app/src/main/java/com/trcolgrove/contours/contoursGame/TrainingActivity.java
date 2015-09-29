package com.trcolgrove.contours.contoursGame;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.os.AsyncTask;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.trcolgrove.colorfulPiano.Piano;
import com.trcolgrove.contours.R;
import com.trcolgrove.contours.activities.EndReportActivity;
import com.trcolgrove.contours.events.GameCompleteEvent;
import com.trcolgrove.contours.events.NoteEvent;
import com.trcolgrove.contours.events.ScoreEvent;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.PdAudio;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;
import org.puredata.core.utils.PdDispatcher;

import java.io.File;
import java.io.IOException;

import de.greenrobot.event.EventBus;
import jp.kshoji.driver.midi.activity.AbstractSingleMidiActivity;
import jp.kshoji.driver.midi.device.MidiInputDevice;
import jp.kshoji.driver.midi.device.MidiOutputDevice;

public class TrainingActivity extends AbstractSingleMidiActivity {

    private int[] multiplierBackgrounds = {R.drawable.multiplierbg_1, R.drawable.multiplierbg_2,
                                            R.drawable.multiplierbg_3, R.drawable.multiplierbg_4,
                                            R.drawable.multiplierbg_5, R.drawable.multiplierbg_6,
                                            R.drawable.multiplierbg_7, R.drawable.multiplierbg_8};

    private static final int MIN_SAMPLE_RATE = 44100;
    private Piano pianoView;
    private ContoursGameView gameView;
    private ProgressBar progressBar;
    private Chronometer chronometer;
    private TextSwitcher scoreSwitcher;
    private TextSwitcher multiplierSwitcher;
    private TextView scoreIncrementText;
    private String patchFilePath;
    private MidiInputDevice midiIn;

    private PowerManager pm;
    PowerManager.WakeLock cpuLock;

    private String TAG = "TrainingActivity";
    @Override
    public void onDeviceAttached(@NonNull UsbDevice usbDevice) {
        // deprecated method.
        // do nothing
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
    public void onMidiNoteOff(@NonNull final MidiInputDevice sender, int cable, int channel, int note, int velocity) {
        pianoView.noteOff(note, velocity);
    }

    @Override
    public void onMidiNoteOn(@NonNull final MidiInputDevice sender, int cable, int channel, int note, int velocity) {
        pianoView.noteOn(note, velocity);
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

    ArrayAdapter<String> midiInputEventAdapter;
    ArrayAdapter<String> midiOutputEventAdapter;

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
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
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

    public void onEvent(final NoteEvent event) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                boolean isFinished = gameView.processMidiInput(event.midiNote);
            }
        });
    }

    public void onEvent(ScoreEvent event) {
        scoreSwitcher.setText(Integer.toString(event.totalScore));
        multiplierSwitcher.setText("x" + Integer.toString(event.multiplier));
        if(android.os.Build.VERSION.SDK_INT > 16) {
            multiplierSwitcher.setBackground(getResources().getDrawable(multiplierBackgrounds[event.multiplier - 1]));
        }
        displayScoreIncrement(event.scoreIncrement);
    }

    public void onEvent(GameCompleteEvent gce) {
        Intent i = new Intent(getApplicationContext(), EndReportActivity.class);
        i.putExtras(gce.scoreBundle);
        startActivity(i);
        this.finish();
    }

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
                try {
                    IoUtils.extractZipResource(getResources().openRawResource(R.raw.testpatch), dir, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;

        }

        protected void onPostExecute(Void v) {

            try {
                PdBase.openPatch(patchFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            progressBar.setVisibility(View.GONE);
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        pianoView = (Piano) findViewById(R.id.piano);
        //staffScroller = (ScrollView) findViewById(R.id.staffScroller);
        gameView = (ContoursGameView) findViewById(R.id.staff);
        progressBar = (ProgressBar) findViewById(R.id.training_loader);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        scoreSwitcher = (TextSwitcher) findViewById(R.id.score_text);
        multiplierSwitcher = (TextSwitcher) findViewById(R.id.multiplier);
        scoreIncrementText = (TextView) findViewById(R.id.score_increment);

        scoreSwitcher.setFactory(scoreTextFactory);
        multiplierSwitcher.setFactory(multiplierTextFactory);

        Animation in = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_out);

        scoreSwitcher.setInAnimation(in);
        scoreSwitcher.setOutAnimation(out);
        multiplierSwitcher.setInAnimation(in);
        multiplierSwitcher.setOutAnimation(out);

        multiplierSwitcher.setCurrentText("x1");
        scoreSwitcher.setCurrentText("0");

        try {
            initPd();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPiano(View view) {
        PdBase.sendList("soundfont", "set", "piano_1");
    }

    public void setAnalogAge(View view) {
        PdBase.sendMessage("soundfont", "set", "analog_age");
    }

    public void setEnigma(View view) {
        PdBase.sendMessage("soundfont", "set", "enigma_flute");
    }

    public void setStrings(View view) {
    }

    public void setBanjo(View view) {
        PdBase.sendMessage("soundfont", "set", "banjo_1");
    }

    private void initPd() throws IOException {
            AudioParameters.init(this);
            int srate = Math.max(MIN_SAMPLE_RATE, AudioParameters.suggestSampleRate());
            PdAudio.initAudio(srate, 0, 2, 1, true);
            File dir = getFilesDir();

            //File patchFile = new File(dir, "base_sampler.pd");
            File patchFile = new File(dir, "contours_patch.pd");

            new resourcesLoader().execute(dir);
            PdDispatcher dispatcher = new PdUiDispatcher();
            PdBase.setReceiver(dispatcher);
            patchFilePath = patchFile.getAbsolutePath();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_training, menu);
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(midiIn != null) {
            midiIn.suspend();
            midiIn = null;
        }
        cleanup();
    }

    private ViewSwitcher.ViewFactory scoreTextFactory = new ViewSwitcher.ViewFactory() {

        @Override
        public View makeView() {
            // Create a new TextView
            TextView t = new TextView(TrainingActivity.this);
            t.setGravity(Gravity.TOP | Gravity.START);
            t.setTextAppearance(TrainingActivity.this, android.R.style.TextAppearance_Large);
            t.setTextColor(Color.WHITE);
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 45);
            t.setMinEms(4);
            t.setMaxEms(4);
            return t;
        }
    };

    private ViewSwitcher.ViewFactory multiplierTextFactory = new ViewSwitcher.ViewFactory() {

        @Override
        public View makeView() {
            // Create a new TextView
            TextView t = new TextView(TrainingActivity.this);
            t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            t.setTextAppearance(TrainingActivity.this, android.R.style.TextAppearance_Large);
            t.setTextColor(Color.WHITE);
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 45);
            t.setMaxEms(2);
            return t;
        }
    };

}