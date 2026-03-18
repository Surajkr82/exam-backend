package com.exam.examportal.controllers;

import com.exam.examportal.models.exam.Attempt;
import com.exam.examportal.models.exam.Quiz;
import com.exam.examportal.services.AttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(path = "/attempt")
public class AttemptController {

    @Autowired
    private AttemptService attemptService;

    @GetMapping(path = "/quiz/{qid}")
    public ResponseEntity<List<Attempt>> getAttemptsOfQuiz(@PathVariable("qid") Long qid) {
        Quiz quiz = new Quiz();
        quiz.setqId(qid);
        return ResponseEntity.ok(this.attemptService.getAttemptsByQuiz(quiz));
    }

    @GetMapping(path = "/{attemptId}")
    public ResponseEntity<Attempt> getAttempt(@PathVariable("attemptId") Long attemptId) {
        return ResponseEntity.ok(this.attemptService.getAttempt(attemptId));
    }
}
