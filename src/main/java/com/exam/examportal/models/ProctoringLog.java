package com.exam.examportal.models;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "proctoring_logs")
public class ProctoringLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // The student
    private Long quizId; // The exam

    @Column(length = 500)
    private String activityType; // e.g. "PHONE_DETECTED", "TAB_SWITCHED"

    private int scoreAdded; // How much score this activity added

    private LocalDateTime timestamp;

    public ProctoringLog() {
    }

    public ProctoringLog(Long userId, Long quizId, String activityType, int scoreAdded, LocalDateTime timestamp) {
        this.userId = userId;
        this.quizId = quizId;
        this.activityType = activityType;
        this.scoreAdded = scoreAdded;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public int getScoreAdded() {
        return scoreAdded;
    }

    public void setScoreAdded(int scoreAdded) {
        this.scoreAdded = scoreAdded;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
