package com.trcolgrove.contours.contoursGame;

/**
 * Basic, simple interface for scorekeeping system
 * Created by Thomas on 7/11/15.
 */
public interface ScoreKeeper {

    public @interface GameEvent {}

    //updates the totalScore base on input from the game
    void updateScore(@GameEvent int scoreVal);
    int getScore();
    int getMultiplier();
}
