package edu.tufts.contours.data;

/**
 * Stores the question and response of a single survey question from the end of the training
 * activity.
 *
 * Can be converted to and from JSON using the gson library
 *
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
