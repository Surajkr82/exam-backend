package com.exam.examportal.models;

import java.util.Map;
import java.util.List;

public class AnalyticsResponse {

    private long activeUsersToday;
    private long totalAttempts;
    private double completionRate;
    private double avgPassScore;
    private Map<String, Long> examsByCategory;
    private double passRatio;
    private double failRatio;

    // For Statistics Page
    private List<TopScorer> topScorers;
    private List<RecentActivity> recentActivities;

    public AnalyticsResponse() {
    }

    public long getActiveUsersToday() {
        return activeUsersToday;
    }

    public void setActiveUsersToday(long activeUsersToday) {
        this.activeUsersToday = activeUsersToday;
    }

    public long getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(long totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(double completionRate) {
        this.completionRate = completionRate;
    }

    public double getAvgPassScore() {
        return avgPassScore;
    }

    public void setAvgPassScore(double avgPassScore) {
        this.avgPassScore = avgPassScore;
    }

    public Map<String, Long> getExamsByCategory() {
        return examsByCategory;
    }

    public void setExamsByCategory(Map<String, Long> examsByCategory) {
        this.examsByCategory = examsByCategory;
    }

    public double getPassRatio() {
        return passRatio;
    }

    public void setPassRatio(double passRatio) {
        this.passRatio = passRatio;
    }

    public double getFailRatio() {
        return failRatio;
    }

    public void setFailRatio(double failRatio) {
        this.failRatio = failRatio;
    }

    public List<TopScorer> getTopScorers() {
        return topScorers;
    }

    public void setTopScorers(List<TopScorer> topScorers) {
        this.topScorers = topScorers;
    }

    public List<RecentActivity> getRecentActivities() {
        return recentActivities;
    }

    public void setRecentActivities(List<RecentActivity> recentActivities) {
        this.recentActivities = recentActivities;
    }

    public static class TopScorer {
        private String studentName;
        private String examName;
        private String scorePercentage;
        private String dateStr;

        public TopScorer(String studentName, String examName, String scorePercentage, String dateStr) {
            this.studentName = studentName;
            this.examName = examName;
            this.scorePercentage = scorePercentage;
            this.dateStr = dateStr;
        }

        public String getStudentName() {
            return studentName;
        }

        public String getExamName() {
            return examName;
        }

        public String getScorePercentage() {
            return scorePercentage;
        }

        public String getDateStr() {
            return dateStr;
        }
    }

    public static class RecentActivity {
        private String title;
        private String description;
        private String timeAgo;
        private String type; // "primary", "success", "warning"

        public RecentActivity(String title, String description, String timeAgo, String type) {
            this.title = title;
            this.description = description;
            this.timeAgo = timeAgo;
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getTimeAgo() {
            return timeAgo;
        }

        public String getType() {
            return type;
        }
    }
}
