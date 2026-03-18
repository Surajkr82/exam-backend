package com.exam.examportal.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.exam.examportal.models.ProctoringLog;

@Repository
public interface ProctoringLogRepository extends JpaRepository<ProctoringLog, Long> {

    // Find logs for a specific user in a specific quiz
    List<ProctoringLog> findByUserIdAndQuizIdOrderByTimestampAsc(Long userId, Long quizId);

    // Find all logs for a specific quiz (useful for admin overview)
    List<ProctoringLog> findByQuizIdOrderByTimestampDesc(Long quizId);
}
