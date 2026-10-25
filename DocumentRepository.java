package com.SpringAi.SpringAi2.repository;


import com.SpringAi.SpringAi2.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    // JpaRepository already provides: save, findAll, findById, existsById, deleteById, etc.
}
