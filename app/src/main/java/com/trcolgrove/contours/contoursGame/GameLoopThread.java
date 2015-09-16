package com.trcolgrove.contours.contoursGame;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.os.BatteryManager;

import aurelienribon.tweenengine.TweenManager;

/**
 * Main game loop for contours game
 *
 * Created by Thomas on 7/5/15.
 */

public class GameLoopThread extends Thread {
    private ContoursGameView contoursGameView;
    private boolean running = false;
    static long FPS = 60;
    private TweenManager tweenManager;
    private boolean dropNextFrame;

    public GameLoopThread(ContoursGameView contoursGameView, TweenManager tweenManager) {
        this.contoursGameView = contoursGameView;
        this.tweenManager = tweenManager;
        if(!connectedToPower(contoursGameView.getContext())) {
            FPS = 40;
        }
    }

    public static boolean connectedToPower(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
    }


    public void setRunning(boolean run) {
        running = run;
    }

    @SuppressLint("WrongCall")
    @Override
    public void run() {

        int frames = 0;
        long lastSec = System.currentTimeMillis();
        long ticksPS = 1000 / FPS;
        long startTime;
        long sleepTime;
        long lastTime = 0;

        while (running) {
            frames++;
            Canvas c = null;
            startTime = System.currentTimeMillis();
            if(startTime > lastSec + 1000) {
                //Log.i("FPS:", Integer.toString(frames));
                frames = 0;
                lastSec = startTime;
            }

            final float delta = (startTime - lastTime)/1000f;
            try {
                c = contoursGameView.getHolder().lockCanvas();

                synchronized (contoursGameView.getHolder()) {
                    tweenManager.update(delta);
                    if(contoursGameView != null) {
                        contoursGameView.onDraw(c);
                    } else {
                        return;
                    }
                }
            } finally {
                if (c != null) {
                    contoursGameView.getHolder().unlockCanvasAndPost(c);
                }
            }
            sleepTime = ticksPS-(System.currentTimeMillis() - startTime);
            try {
                if (sleepTime > 0) {
                    sleep(sleepTime);
                } else {
                    sleep(10);
                }
            } catch (Exception e) {}
            lastTime = startTime;
        }
    }
}
