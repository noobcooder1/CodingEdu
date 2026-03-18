package com.codingedu.repository;

import com.codingedu.entity.QuizResult;
import com.codingedu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findByUserOrderByCreatedAtDesc(User user);
}
