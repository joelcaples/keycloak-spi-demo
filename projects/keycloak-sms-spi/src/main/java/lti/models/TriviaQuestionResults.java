package lti.models;

import java.util.ArrayList;

// import com.fasterxml.jackson.annotation.JsonProperty;

public class TriviaQuestionResults{
    public int response_code;

    // @JsonProperty("questions")
    public ArrayList<TriviaQuestion> results;
}
