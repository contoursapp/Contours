package com.trcolgrove.contours.contoursGame;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.util.Log;

import aurelienribon.tweenengine.TweenManager;

/**
 * Main game loop for contours game
 *
 * Created by Thomas on 7/5/15.
 */

public class GameLoopThread extends Thread {
    private ContoursGameView contoursGameView;
    private boolean running = false;
    static final long FPS = 48;
    private TweenManager tweenManager;

    public GameLoopThread(ContoursGameView contoursGameView, TweenManager tweenManager) {
        this.contoursGameView = contoursGameView;
        this.tweenManager = tweenManager;
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
                Log.i("FPS:", Integer.toString(frames));
                frames = 0;
                lastSec = startTime;
            }

            final float delta = (startTime - lastTime)/1000f;
            try {
                c = contoursGameView.getHolder().lockCanvas();
                synchronized (contoursGameView.getHolder()) {
                    tweenManager.update(delta);
                    contoursGameView.onDraw(c);
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
                }
                else {
                    sleep(10);
                }
            } catch (Exception e) {}
            lastTime = startTime;
        }
    }
}
