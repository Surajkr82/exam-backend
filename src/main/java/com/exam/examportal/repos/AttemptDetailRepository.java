package com.exam.examportal.repos;

import com.exam.examportal.models.exam.Attempt;
import com.exam.examportal.models.exam.AttemptDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttemptDetailRepository extends JpaRepository<AttemptDetail, Long> {
    List<AttemptDetail> findByAttempt(Attempt attempt);
}
