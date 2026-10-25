package com.SpringAi.SpringAi2.repository;

import com.SpringAi.SpringAi2.model.QuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, Long> {
    List<QuestionAnswer> findByDocumentIdOrderByAskedAtDesc(Long documentId);
}
