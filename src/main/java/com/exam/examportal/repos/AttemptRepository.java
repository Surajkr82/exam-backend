package com.exam.examportal.repos;

import com.exam.examportal.models.User;
import com.exam.examportal.models.exam.Attempt;
import com.exam.examportal.models.exam.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    List<Attempt> findByQuiz(Quiz quiz);

    List<Attempt> findByUser(User user);
}
