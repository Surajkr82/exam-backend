package com.exam.examportal.controllers;

import com.exam.examportal.models.AnalyticsResponse;
import com.exam.examportal.models.exam.Attempt;
import com.exam.examportal.repos.AttemptRepository;
import com.exam.examportal.repos.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/admin/analytics")
@CrossOrigin(origins = "http://localhost:4200")
public class AnalyticsController {

    @Autowired
    private AttemptRepository attemptRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public ResponseEntity<AnalyticsResponse> getAnalytics() {
        List<Attempt> allAttempts = attemptRepository.findAll();

        AnalyticsResponse response = new AnalyticsResponse();
        response.setTotalAttempts(allAttempts.size());

        // Active Users Today (unique users who made an attempt today)
        LocalDate today = LocalDate.now();
        long activeUsers = allAttempts.stream()
                .filter(a -> {
                    if (a.getAttemptDate() == null)
                        return false;
                    LocalDate attemptDate = a.getAttemptDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return attemptDate.isEqual(today);
                })
                .filter(a -> a.getUser() != null)
                .map(a -> a.getUser().getId())
                .distinct()
                .count();
        response.setActiveUsersToday(activeUsers);

        // Avg Pass Score & Completion Rate
        double totalScorePercent = 0;
        int passCount = 0;
        int completedCount = 0;

        for (Attempt a : allAttempts) {
            if (a.getMaxMarks() > 0) {
                double percent = (a.getMarksGot() / a.getMaxMarks()) * 100;
                totalScorePercent += percent;
                if (percent >= 40.0) { // Assuming 40% is pass
                    passCount++;
                }
            }
            if (a.getAttempted() > 0) {
                completedCount++;
            }
        }

        if (!allAttempts.isEmpty()) {
            response.setAvgPassScore(Math.round(totalScorePercent / allAttempts.size()));
            response.setCompletionRate(Math.round(((double) completedCount / allAttempts.size()) * 100));
            response.setPassRatio(Math.round(((double) passCount / allAttempts.size()) * 100));
            response.setFailRatio(100 - response.getPassRatio());
        } else {
            response.setAvgPassScore(0);
            response.setCompletionRate(0);
            response.setPassRatio(0);
            response.setFailRatio(0);
        }

        // Exams by Category
        Map<String, Long> categoryMap = allAttempts.stream()
                .filter(a -> a.getQuiz() != null && a.getQuiz().getCategory() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getQuiz().getCategory().getTitle(),
                        Collectors.counting()));
        response.setExamsByCategory(categoryMap);

        // Top Scorers
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        List<AnalyticsResponse.TopScorer> topScorers = allAttempts.stream()
                .filter(a -> a.getMaxMarks() > 0 && a.getUser() != null && a.getQuiz() != null)
                .sorted((a1, a2) -> Double.compare(
                        (a2.getMarksGot() / a2.getMaxMarks()),
                        (a1.getMarksGot() / a1.getMaxMarks())))
                .limit(5)
                .map(a -> new AnalyticsResponse.TopScorer(
                        a.getUser().getFirstname() + " " + a.getUser().getLastname(),
                        a.getQuiz().getTitle(),
                        String.format("%.1f%%", (a.getMarksGot() / a.getMaxMarks()) * 100),
                        a.getAttemptDate() != null ? sdf.format(a.getAttemptDate()) : "N/A"))
                .collect(Collectors.toList());
        response.setTopScorers(topScorers);

        // Recent Activity
        List<AnalyticsResponse.RecentActivity> recentActivities = allAttempts.stream()
                .filter(a -> a.getAttemptDate() != null && a.getUser() != null && a.getQuiz() != null)
                .sorted((a1, a2) -> a2.getAttemptDate().compareTo(a1.getAttemptDate()))
                .limit(5)
                .map(a -> {
                    String name = a.getUser().getFirstname() + " " + a.getUser().getLastname();
                    String quiz = a.getQuiz().getTitle();
                    long diffHours = (new Date().getTime() - a.getAttemptDate().getTime()) / (60 * 60 * 1000);
                    String timeStr = diffHours == 0 ? "Just now" : diffHours + " hours ago";

                    return new AnalyticsResponse.RecentActivity(
                            "Exam Submitted",
                            name + " submitted " + quiz,
                            timeStr,
                            "primary");
                })
                .collect(Collectors.toList());
        response.setRecentActivities(recentActivities);

        return ResponseEntity.ok(response);
    }
}
