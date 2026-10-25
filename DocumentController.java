package com.SpringAi.SpringAi2.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import com.SpringAi.SpringAi2.Dto.DocumentUploadResponse;
import com.SpringAi.SpringAi2.Dto.DocumentResponse;
import com.SpringAi.SpringAi2.Dto.QuestionAnswerResponse;
import com.SpringAi.SpringAi2.Dto.QuestionRequest;
import com.SpringAi.SpringAi2.service.DocumentAnalyzerService;



@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DocumentController {

    private final DocumentAnalyzerService documentAnalyzerService;

    /**
     * Upload a new document (PDF, DOCX, TXT)
     * POST /api/documents/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file) {
        try {
            DocumentUploadResponse response = documentAnalyzerService.uploadDocument(file);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new DocumentUploadResponse(null, null, null, null, e.getMessage(), null)
            );
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new DocumentUploadResponse(null, null, null, null,
                            "Failed to upload document: " + e.getMessage(), null)
            );
        }
    }

    /**
     * Get all uploaded documents
     * GET /api/documents
     */
    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAllDocuments() {
        List<DocumentResponse> documents = documentAnalyzerService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }

    /**
     * Get a specific document by ID
     * GET /api/documents/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable Long id) {
        try {
            DocumentResponse response = documentAnalyzerService.getDocument(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Generate AI summary for a document
     * POST /api/documents/{id}/summarize
     */
    @PostMapping("/{id}/summarize")
    public ResponseEntity<String> summarizeDocument(@PathVariable Long id) {
        try {
            String summary = documentAnalyzerService.generateSummary(id);
            return ResponseEntity.ok(summary);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Ask a question about a document
     * POST /api/documents/{id}/ask
     * Body: {"question": "What is this document about?"}
     */
    @PostMapping("/{id}/ask")
    public ResponseEntity<QuestionAnswerResponse> askQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequest request) {
        try {
            QuestionAnswerResponse response = documentAnalyzerService.askQuestion(id, request.getQuestion());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all questions and answers for a document
     * GET /api/documents/{id}/questions
     */
    @GetMapping("/{id}/questions")
    public ResponseEntity<List<QuestionAnswerResponse>> getQuestionHistory(@PathVariable Long id) {
        try {
            List<QuestionAnswerResponse> history = documentAnalyzerService.getQuestionHistory(id);
            return ResponseEntity.ok(history);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a document
     * DELETE /api/documents/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        try {
            documentAnalyzerService.deleteDocument(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}