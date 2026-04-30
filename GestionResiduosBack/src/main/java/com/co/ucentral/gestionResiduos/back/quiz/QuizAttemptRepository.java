package com.co.ucentral.gestionResiduos.back.quiz;

import com.co.ucentral.gestionResiduos.back.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByUserAndQuiz(User user, Quiz quiz);
    List<QuizAttempt> findByUserOrderByCompletedAtDesc(User user);
    boolean existsByUserAndQuiz(User user, Quiz quiz);
}