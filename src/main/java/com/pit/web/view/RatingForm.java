package com.pit.web.view;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

// Application component.
public class RatingForm {

    @Min(1)
    @Max(5)
    private Integer score;

    @Size(max = 1000)
    private String comment;

    // Handles get score request operation
    public Integer getScore() {
        return score;
    }

    // Handles set score request operation
    public void setScore(Integer score) {
        this.score = score;
    }

    // Handles get comment request operation
    public String getComment() {
        return comment;
    }

    // Handles set comment request operation
    public void setComment(String comment) {
        this.comment = comment;
    }
}
