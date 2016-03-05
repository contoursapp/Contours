package com.trcolgrove.daogenerator;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class ContoursDaoGenerator {

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(1, "com.trcolgrove.daoentries");
        addScoreSet(schema);
        addSurveyResponse(schema);
        new DaoGenerator().generateAll(schema, "../app/src-gen");
    }

    private static void addScoreSet(Schema schema) {
        Entity scoreSet = schema.addEntity("ScoreSet");
        scoreSet.addIdProperty();
        scoreSet.addStringProperty("user_id").notNull();
        scoreSet.addStringProperty("difficulty");
        scoreSet.addIntProperty("total_score").notNull();
        scoreSet.addLongProperty("elapsed_time");
        scoreSet.addIntProperty("notes_hit");
        scoreSet.addIntProperty("notes_missed");
        scoreSet.addIntProperty("longest_streak");
        scoreSet.addIntProperty("average_streak");
        scoreSet.addDateProperty("date");
        scoreSet.addBooleanProperty("uploaded").notNull();
    }

    private static void addSurveyResponse(Schema schema) {
        Entity surveyResponse = schema.addEntity("SurveyResponse");
        surveyResponse.addIdProperty().autoincrement();
        surveyResponse.addStringProperty("question");
        surveyResponse.addIntProperty("response");
        surveyResponse.addDateProperty("date");
        surveyResponse.addBooleanProperty("uploaded").notNull();
    }

}