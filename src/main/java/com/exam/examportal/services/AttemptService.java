package com.exam.examportal.services;

import com.exam.examportal.models.User;
import com.exam.examportal.models.exam.Attempt;
import com.exam.examportal.models.exam.Quiz;

import java.util.List;

public interface AttemptService {
    public Attempt addAttempt(Attempt attempt);

    public List<Attempt> getAttemptsByQuiz(Quiz quiz);

    public List<Attempt> getAttemptsByUser(User user);

    public Attempt getAttempt(Long attemptId);
}
