package com.exam.examportal.controllers;

import com.exam.examportal.models.User;
import com.exam.examportal.models.exam.Attempt;
import com.exam.examportal.models.exam.AttemptDetail;
import com.exam.examportal.models.exam.Question;
import com.exam.examportal.models.exam.Quiz;
import com.exam.examportal.repos.UserRepository;
import com.exam.examportal.services.AiGradingService;
import com.exam.examportal.services.AttemptService;
import com.exam.examportal.services.QuestionService;
import com.exam.examportal.services.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(path = "/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuizService quizService;

    @Autowired
    private AttemptService attemptService;

    @Autowired
    private AiGradingService aiGradingService;

    @Autowired
    private UserRepository userRepository;

    // add Question
    @PostMapping(path = "/")
    public ResponseEntity<Question> add(@RequestBody Question question) {
        return ResponseEntity.ok(this.questionService.addQuestion(question));
    }

    // update Question
    @PutMapping(path = "/")
    public ResponseEntity<Question> update(@RequestBody Question question) {
        return ResponseEntity.ok(this.questionService.updateQuestion(question));
    }

    // Get all question of any quiz
    @GetMapping(path = "/quiz/{qid}")
    public ResponseEntity<?> getQuestionsOfQuiz(@PathVariable("qid") Long qid) {
        // Quiz quiz = new Quiz();
        // quiz.setqId(qid);
        // Set<Question> questionsOfQuiz =
        // this.questionService.getQuestionsOfQuiz(quiz);
        // return ResponseEntity.ok(questionsOfQuiz);

        Quiz quiz = this.quizService.getQuiz(qid);
        Set<Question> questions = quiz.getQuestionSet();

        List<Question> list = new ArrayList<Question>(questions);

        if (list.size() > Integer.parseInt(quiz.getNoOfQuestions())) {
            list = list.subList(0, Integer.parseInt(quiz.getNoOfQuestions() + 1));
        }

        list.forEach((q) -> {
            q.setAnswer("");
        });

        Collections.shuffle(list);
        return ResponseEntity.ok(list);
    }

    @GetMapping(path = "/quiz/all/{qid}")
    public ResponseEntity<?> getQuestionsOfQuizAdmin(@PathVariable("qid") Long qid) {

        Quiz quiz = new Quiz();
        quiz.setqId(qid);
        Set<Question> questionsOfQuiz = this.questionService.getQuestionsOfQuiz(quiz);
        return ResponseEntity.ok(questionsOfQuiz);
    }

    // get single question
    @GetMapping(path = "/{quesId}")
    public Question get(@PathVariable("quesId") Long quesId) {
        return this.questionService.getQuestion(quesId);
    }

    // Delete question
    @DeleteMapping(path = "/{quesId}")
    public void delete(@PathVariable("quesId") Long quesId) {
        this.questionService.deleteQuestion(quesId);
    }

    // Evaluate Quiz
    @PostMapping(path = "/eval-quiz")
    public ResponseEntity<?> evalQuiz(@RequestBody List<Question> questions, Principal principal) {
        double marksGot = 0.0;
        Integer correctAnswers = 0;
        Integer attempted = 0;
        double cheatingRiskScore = 0.0;

        if (questions != null && !questions.isEmpty()) {
            cheatingRiskScore = questions.get(0).getCheatingRiskScore();
        }

        User user = null;
        if (principal != null) {
            user = this.userRepository.findByUsername(principal.getName());
        }

        Attempt attempt = new Attempt();
        attempt.setUser(user);
        attempt.setCheatingRiskScore(cheatingRiskScore);

        Quiz requestQuiz = questions.get(0).getQuiz();
        Quiz quiz = this.quizService.getQuiz(requestQuiz.getqId());
        attempt.setQuiz(quiz);
        attempt.setMaxMarks(Double.parseDouble(quiz.getMaxMarks()));

        double singleMarks = Double.parseDouble(quiz.getMaxMarks()) / Double.parseDouble(quiz.getNoOfQuestions());

        Set<AttemptDetail> details = new HashSet<>();
        List<Map<String, Object>> evaluatedList = new java.util.ArrayList<>();

        for (Question q : questions) {
            Question question = this.questionService.get(q.getQuesId());
            AttemptDetail detail = new AttemptDetail();
            detail.setAttempt(attempt);
            detail.setQuestion(question);
            detail.setGivenAnswer(q.getGivenAnswer());

            double awarded = 0;

            if (q.getGivenAnswer() != null && !q.getGivenAnswer().trim().isEmpty()) {
                attempted++;
                if (question.getType() != null && question.getType().equals("SUBJECTIVE")) {
                    double aiMarks = this.aiGradingService.gradeSubjectiveQuestion(question.getContent(),
                            q.getGivenAnswer(), singleMarks);
                    awarded = aiMarks;
                    marksGot += aiMarks;
                } else {
                    if (question.getAnswer() != null && question.getAnswer().equals(q.getGivenAnswer())) {
                        correctAnswers++;
                        awarded = singleMarks;
                        marksGot += singleMarks;
                    }
                }
            }
            detail.setAwardedMarks(awarded);
            details.add(detail);

            Map<String, Object> evalMap = new java.util.HashMap<>();
            evalMap.put("quesId", question.getQuesId());
            evalMap.put("answer", question.getAnswer());
            evalMap.put("awardedMarks", awarded);
            evaluatedList.add(evalMap);
        }

        attempt.setMarksGot(marksGot);
        attempt.setCorrectAnswers(correctAnswers);
        attempt.setAttempted(attempted);
        attempt.setAttemptDetails(details);

        if (user != null) {
            this.attemptService.addAttempt(attempt);
        }

        Map<String, Object> map = new java.util.HashMap<>();
        map.put("marksGot", marksGot);
        map.put("correctAnswers", correctAnswers);
        map.put("attempted", attempted);
        map.put("evaluatedQuestions", evaluatedList);
        return ResponseEntity.ok(map);
    }
}
