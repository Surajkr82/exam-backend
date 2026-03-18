package com.exam.examportal.services.impl;

import com.exam.examportal.models.User;
import com.exam.examportal.models.exam.Attempt;
import com.exam.examportal.models.exam.Quiz;
import com.exam.examportal.repos.AttemptRepository;
import com.exam.examportal.services.AttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttemptServiceImpl implements AttemptService {

    @Autowired
    private AttemptRepository attemptRepository;

    @Override
    public Attempt addAttempt(Attempt attempt) {
        return this.attemptRepository.save(attempt);
    }

    @Override
    public List<Attempt> getAttemptsByQuiz(Quiz quiz) {
        return this.attemptRepository.findByQuiz(quiz);
    }

    @Override
    public List<Attempt> getAttemptsByUser(User user) {
        return this.attemptRepository.findByUser(user);
    }

    @Override
    public Attempt getAttempt(Long attemptId) {
        return this.attemptRepository.findById(attemptId).orElse(null);
    }
}
