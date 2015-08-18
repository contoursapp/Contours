package com.trcolgrove.contours.contoursGame;

import java.util.Map;

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

    /**
     * Returns the information kept in the ScoreKeeper object
     * as a Map. For example, the map might contain
     * the pair "total_score" -> 9000(int) "total_time"->3.56(Duration)
     *
     * @return a map representing the score keepers metrics and their values
     */
    Map<String,Object> getScoreMap();
}
