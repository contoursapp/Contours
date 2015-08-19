package com.trcolgrove.contours.contoursGame;

import android.content.Context;

import com.trcolgrove.contours.R;
import com.trcolgrove.daoentries.ScoreSet;

/**
 * Singleton class for interfacing
 * with the server
 *
 *
 * Created by Thomas on 8/19/15.
 */
public class ServerUtil {
    protected String servAddr;

    public void ServerUtil(Context context) {
        servAddr = context.getString(R.string.servAddr);
    }

    public static void postScoreSet(ScoreSet scoreSet) {
        //TODO: connect to server and post information
    }
}
