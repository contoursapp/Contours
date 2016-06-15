package edu.tufts.contours.contoursGame;

/**
 * Created by Thomas on 3/14/16.
 */
public class SurveyResponse {

    private String question;
    private String response;

    public SurveyResponse(String question, String response) {
        this.question = question;
        this.response = response;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

}
