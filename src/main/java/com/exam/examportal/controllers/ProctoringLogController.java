package com.exam.examportal.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exam.examportal.models.ProctoringLog;
import com.exam.examportal.repos.ProctoringLogRepository;

@RestController
@RequestMapping("/proctoring")
@CrossOrigin("*")
public class ProctoringLogController {

    @Autowired
    private ProctoringLogRepository proctoringLogRepository;

    // Submit a new log event
    @PostMapping("/log")
    public ResponseEntity<ProctoringLog> saveLog(@RequestBody ProctoringLog log) {
        log.setTimestamp(LocalDateTime.now());
        ProctoringLog savedLog = this.proctoringLogRepository.save(log);
        return ResponseEntity.ok(savedLog);
    }

    // Get logs for a specific user and quiz
    @GetMapping("/logs/{quizId}/{userId}")
    public ResponseEntity<List<ProctoringLog>> getUserLogs(@PathVariable("quizId") Long quizId,
            @PathVariable("userId") Long userId) {
        List<ProctoringLog> logs = this.proctoringLogRepository.findByUserIdAndQuizIdOrderByTimestampAsc(userId,
                quizId);
        return ResponseEntity.ok(logs);
    }

    // Get all logs for a specific quiz
    @GetMapping("/logs/{quizId}")
    public ResponseEntity<List<ProctoringLog>> getQuizLogs(@PathVariable("quizId") Long quizId) {
        List<ProctoringLog> logs = this.proctoringLogRepository.findByQuizIdOrderByTimestampDesc(quizId);
        return ResponseEntity.ok(logs);
    }
}
