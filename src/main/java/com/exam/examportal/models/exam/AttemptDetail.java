package com.exam.examportal.models.exam;

import javax.persistence.*;

@Entity
public class AttemptDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long attemptDetailId;

    @ManyToOne(fetch = FetchType.EAGER)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Attempt attempt;

    @ManyToOne(fetch = FetchType.EAGER)
    private Question question;

    @Column(length = 5000)
    private String givenAnswer;

    private double awardedMarks;

    public AttemptDetail() {
    }

    public Long getAttemptDetailId() {
        return attemptDetailId;
    }

    public void setAttemptDetailId(Long attemptDetailId) {
        this.attemptDetailId = attemptDetailId;
    }

    public Attempt getAttempt() {
        return attempt;
    }

    public void setAttempt(Attempt attempt) {
        this.attempt = attempt;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getGivenAnswer() {
        return givenAnswer;
    }

    public void setGivenAnswer(String givenAnswer) {
        this.givenAnswer = givenAnswer;
    }

    public double getAwardedMarks() {
        return awardedMarks;
    }

    public void setAwardedMarks(double awardedMarks) {
        this.awardedMarks = awardedMarks;
    }
}
