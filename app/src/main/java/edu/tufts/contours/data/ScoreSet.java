package edu.tufts.contours.data;

import java.util.ArrayList;

/**
 * Class keeping track of the data from one contours session
 * can be converted to and from a JSON object using gson library
 */
public class ScoreSet {

    /** Not-null value. */
    private String user_id;
    private String difficulty;
    private int interval_size;
    private int total_score;
    private Long elapsed_time;
    private Integer notes_hit;
    private Integer notes_missed;
    private Integer longest_streak;
    private Integer average_streak;
    private java.util.Date date;
    private ArrayList<ScoreSingle> singles;
    private ArrayList<SurveyResponse> surveyResponses;

    public ScoreSet() {
    }

    public ScoreSet(String user_id, String difficulty, int interval_size, int total_score,
                    Long elapsed_time, Integer notes_hit, Integer notes_missed, Integer longest_streak,
                    Integer average_streak, java.util.Date date, ArrayList<ScoreSingle> singles) {
        this.user_id = user_id;
        this.difficulty = difficulty;
        this.interval_size = interval_size;
        this.total_score = total_score;
        this.elapsed_time = elapsed_time;
        this.notes_hit = notes_hit;
        this.notes_missed = notes_missed;
        this.longest_streak = longest_streak;
        this.average_streak = average_streak;
        this.date = date;
        this.singles = singles;
        this.surveyResponses = new ArrayList<>();
    }

    /** Not-null value. */
    public String getUser_id() {
        return user_id;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getTotal_score() {
        return total_score;
    }

    public void setTotal_score(int total_score) {
        this.total_score = total_score;
    }

    public Long getElapsed_time() {
        return elapsed_time;
    }

    public void setElapsed_time(Long elapsed_time) {
        this.elapsed_time = elapsed_time;
    }

    public Integer getNotes_hit() {
        return notes_hit;
    }

    public void setNotes_hit(Integer notes_hit) {
        this.notes_hit = notes_hit;
    }

    public Integer getNotes_missed() {
        return notes_missed;
    }

    public void setNotes_missed(Integer notes_missed) {
        this.notes_missed = notes_missed;
    }

    public Integer getLongest_streak() {
        return longest_streak;
    }

    public void setLongest_streak(Integer longest_streak) {
        this.longest_streak = longest_streak;
    }

    public Integer getAverage_streak() {
        return average_streak;
    }

    public void setAverage_streak(Integer average_streak) {
        this.average_streak = average_streak;
    }

    public java.util.Date getDate() {
        return date;
    }

    public void setDate(java.util.Date date) {
        this.date = date;
    }

    public ArrayList<ScoreSingle> getSingles() {
        return singles;
    }

    public void setSingles(ArrayList<ScoreSingle> singles) {
        this.singles = singles;
    }

    public ArrayList<SurveyResponse> getSurveyResponses() {
        return surveyResponses;
    }

    public void setSurveyResponses(ArrayList<SurveyResponse> surveyResponses) {
        this.surveyResponses = surveyResponses;
    }

    public int getInterval_size() {
        return interval_size;
    }

    public void setInterval_size(int interval_size) {
        this.interval_size = interval_size;
    }

}

