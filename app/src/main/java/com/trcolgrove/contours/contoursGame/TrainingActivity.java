package com.trcolgrove.contours.contoursGame;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.noisepages.nettoyeur.midi.MidiReceiver;
import com.noisepages.nettoyeur.usb.ConnectionFailedException;
import com.noisepages.nettoyeur.usb.DeviceNotConnectedException;
import com.noisepages.nettoyeur.usb.InterfaceNotAvailableException;
import com.noisepages.nettoyeur.usb.UsbBroadcastHandler;
import com.noisepages.nettoyeur.usb.midi.UsbMidiDevice;
import com.noisepages.nettoyeur.usb.midi.util.UsbMidiInputSelector;
import com.noisepages.nettoyeur.usb.midi.util.UsbMidiOutputSelector;
import com.noisepages.nettoyeur.usb.util.AsyncDeviceInfoLookup;
import com.noisepages.nettoyeur.usb.util.UsbDeviceSelector;
import com.trcolgrove.colorfulPiano.Piano;
import com.trcolgrove.contours.R;
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
import java.util.List;

import de.greenrobot.event.EventBus;

public class TrainingActivity extends Activity {

    //TODO: replace current midi implementation with btmidi

    private UsbMidiDevice midiDevice = null;
    private MidiReceiver midiOut = null;
    private Toast toast = null;

    private void toast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (toast == null) {
                    toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
                }
                toast.setText("Contours: " + msg);
                toast.show();
            }
        });
    }

    private final MidiReceiver receiver = new MidiReceiver() {
        @Override
        public void onNoteOn(int channel, int key, final int velocity) {
            final int midiVal = key;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (velocity > 0)
                        pianoView.noteOn(midiVal, velocity);
                    else
                        pianoView.noteOff(midiVal, velocity);
                }
            });
        }

        @Override
        public void onNoteOff(int channel, int key, final int velocity) {
            final int midiVal = key;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pianoView.noteOff(midiVal, velocity);
                }
            });
        }

        @Override
        public void onProgramChange(int channel, int program) {
            toast("program change: " + channel + ", " + program);
        }

        @Override
        public void onPolyAftertouch(int channel, int key, int velocity) {
            toast("aftertouch: " + channel + ", " + key + ", " + velocity);
        }

        @Override
        public void onPitchBend(int channel, int value) {
            toast("pitch bend: " + channel + ", " + value);
        }

        @Override
        public void onControlChange(int channel, int controller, int value) {
            toast("control change: " + channel + ", " + controller + ", " + value);
        }

        @Override
        public void onAftertouch(int channel, int velocity) {
            toast("aftertouch: " + channel + ", " + velocity);
        }

        @Override
        public void onRawByte(byte value) {
            toast("raw byte: " + value);
        }

        @Override
        public boolean beginBlock() {
            return false;
        }

        @Override
        public void endBlock() {}
    };

    private static final int MIN_SAMPLE_RATE = 44100;

    final Handler midiInputEventHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (midiInputEventAdapter != null) {
                midiInputEventAdapter.add((String) msg.obj);
            }
            // message handled successfully
            return true;
        }
    });

    final Handler midiOutputEventHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (midiOutputEventAdapter != null) {
                midiOutputEventAdapter.add((String) msg.obj);
            }
            // message handled successfully
            return true;
        }
    });

    ArrayAdapter<String> midiInputEventAdapter;
    ArrayAdapter<String> midiOutputEventAdapter;
    private Piano pianoView;
    private ContoursGameView gameView;
    private ProgressBar progressBar;
    private Chronometer chronometer;
    private TextSwitcher scoreSwitcher;
    private TextSwitcher multiplierSwitcher;
    private TextView scoreIncrementText;

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStart() {
        super.onStart();
        PdAudio.startAudio(this);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        PdAudio.stopAudio();
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(NoteEvent event) {
        gameView.checkNote(event.midiNote);
    }

    public void onEvent(ScoreEvent event) {
        scoreSwitcher.setText(Integer.toString(event.totalScore));
        multiplierSwitcher.setText("x" + Integer.toString(event.multiplier));
        displayScoreIncrement(event.scoreIncrement);
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
        // make sure to release all resources
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

        UsbMidiDevice.installBroadcastHandler(this, new UsbBroadcastHandler() {

            @Override
            public void onPermissionGranted(UsbDevice device) {
                if (midiDevice == null || !midiDevice.matches(device)) return;
                try {
                    midiDevice.open(TrainingActivity.this);
                } catch (ConnectionFailedException e) {
                    toast("USB connection failed");
                    midiDevice = null;
                    return;
                }
                final UsbMidiOutputSelector outputSelector = new UsbMidiOutputSelector(midiDevice) {

                    @Override
                    protected void onOutputSelected(UsbMidiDevice.UsbMidiOutput output, UsbMidiDevice device, int iface,
                                                    int index) {
                        toast("Output selection: Interface " + iface + ", Output " + index);
                        try {
                            midiOut = output.getMidiOut();
                        } catch (DeviceNotConnectedException e) {
                            toast("MIDI device has been disconnected");
                        } catch (InterfaceNotAvailableException e) {
                            toast("MIDI interface is unavailable");
                        }
                    }

                    @Override
                    protected void onNoSelection(UsbMidiDevice device) {
                        toast("No output selected");
                    }
                };
                new UsbMidiInputSelector(midiDevice) {

                    @Override
                    protected void onInputSelected(UsbMidiDevice.UsbMidiInput input, UsbMidiDevice device, int iface,
                                                   int index) {
                        toast("Input selection: Interface " + iface + ", Input " + index);
                        input.setReceiver(receiver);
                        try {
                            input.start();
                        } catch (DeviceNotConnectedException e) {
                            toast("MIDI device has been disconnected");
                            return;
                        } catch (InterfaceNotAvailableException e) {
                            toast("MIDI interface is unavailable");
                            return;
                        }
                        outputSelector.show(getFragmentManager(), null);
                    }

                    @Override
                    protected void onNoSelection(UsbMidiDevice device) {
                        toast("No input selected");
                        outputSelector.show(getFragmentManager(), null);
                    }
                }.show(getFragmentManager(), null);
            }

            @Override
            public void onPermissionDenied(UsbDevice device) {
                if (midiDevice == null || !midiDevice.matches(device)) return;
                toast("Permission denied for device " + midiDevice.getCurrentDeviceInfo());
                midiDevice = null;
            }

            @Override
            public void onDeviceDetached(UsbDevice device) {
                if (midiDevice == null || !midiDevice.matches(device)) return;
                midiDevice.close();
                midiDevice = null;
                toast("MIDI device disconnected");
            }
        });

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
        chooseMidiDevice();
    }

    public void setBanjo(View view) {
        PdBase.sendMessage("soundfont", "set", "banjo_1");
    }

    private void initPd() throws IOException {
            AudioParameters.init(this);
            int srate = Math.max(MIN_SAMPLE_RATE, AudioParameters.suggestSampleRate());
            PdAudio.initAudio(srate, 0, 2, 1, true);
            File dir = getFilesDir();
            File patchFile = new File(dir, "base_sampler.pd");
            new resourcesLoader().execute(dir);
            PdDispatcher dispatcher = new PdUiDispatcher();
            PdBase.setReceiver(dispatcher);
            PdBase.openPatch(patchFile.getAbsolutePath());
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

    private void chooseMidiDevice() {
        final List<UsbMidiDevice> devices = UsbMidiDevice.getMidiDevices(this);
        new AsyncDeviceInfoLookup() {

            @Override
            protected void onLookupComplete() {
                new UsbDeviceSelector<UsbMidiDevice>(devices) {

                    @Override
                    protected void onDeviceSelected(UsbMidiDevice device) {
                        midiDevice = device;
                        midiDevice.requestPermission(TrainingActivity.this);
                    }

                    @Override
                    protected void onNoSelection() {
                        toast("No device selected");
                    }
                }.show(getFragmentManager(), null);
            }
        }.execute(devices.toArray(new UsbMidiDevice[devices.size()]));
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
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