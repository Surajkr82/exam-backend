package com.exam.examportal.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiGradingService {

    @Autowired
    private AiClientService aiClientService;

    private static final Pattern SCORE_PATTERN = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)");

    public double gradeSubjectiveQuestion(String questionContent, String answerContent, double maxMarks) {
        String prompt = "You are an expert AI grader. Evaluate the following subjective answer to the given question.\n"
                +
                "Question: " + questionContent + "\n" +
                "User Answer: " + answerContent + "\n" +
                "Max Marks: " + maxMarks + "\n" +
                "Respond ONLY with a number representing the score out of " + maxMarks
                + ". Do not include any other text, explanation, or context. Just the number.";

        try {
            String resultText = aiClientService.generateText(prompt, 0.1, 128, null);
            if (resultText == null || resultText.trim().isEmpty()) {
                return 0;
            }

            Matcher matcher = SCORE_PATTERN.matcher(resultText);
            if (!matcher.find()) {
                return 0;
            }

            double score = Double.parseDouble(matcher.group(1));
            if (score < 0) {
                return 0;
            }
            if (score > maxMarks) {
                return maxMarks;
            }
            return score;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0; // default to 0 if ranking fails
    }
}
