package com.exam.examportal.models.exam;

import com.exam.examportal.models.User;
import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Attempt {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long attemptId;

    @ManyToOne(fetch = FetchType.EAGER)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    private double marksGot;
    private int correctAnswers;
    private int attempted;
    private double maxMarks;
    private double cheatingRiskScore = 0.0;

    private Date attemptDate = new Date();

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<AttemptDetail> attemptDetails = new HashSet<>();

    public Attempt() {
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getMarksGot() {
        return marksGot;
    }

    public void setMarksGot(double marksGot) {
        this.marksGot = marksGot;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getAttempted() {
        return attempted;
    }

    public void setAttempted(int attempted) {
        this.attempted = attempted;
    }

    public double getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(double maxMarks) {
        this.maxMarks = maxMarks;
    }

    public Date getAttemptDate() {
        return attemptDate;
    }

    public void setAttemptDate(Date attemptDate) {
        this.attemptDate = attemptDate;
    }

    public Set<AttemptDetail> getAttemptDetails() {
        return attemptDetails;
    }

    public void setAttemptDetails(Set<AttemptDetail> attemptDetails) {
        this.attemptDetails = attemptDetails;
    }

    public double getCheatingRiskScore() {
        return cheatingRiskScore;
    }

    public void setCheatingRiskScore(double cheatingRiskScore) {
        this.cheatingRiskScore = cheatingRiskScore;
    }
}
