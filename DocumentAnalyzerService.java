package com.SpringAi.SpringAi2.service;


import com.SpringAi.SpringAi2.Dto.*;
import com.SpringAi.SpringAi2.model.Document;
import com.SpringAi.SpringAi2.model.QuestionAnswer;
import com.SpringAi.SpringAi2.repository.DocumentRepository;
import com.SpringAi.SpringAi2.repository.QuestionAnswerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentAnalyzerService {

    private final DocumentRepository documentRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final ChatClient chatClient;

    private static final List<String> ALLOWED_TYPES = List.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * Upload and process a document
     */
    @Transactional
    public DocumentUploadResponse uploadDocument(MultipartFile file) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported file type. Please upload PDF, DOCX, or TXT files.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 10MB");
        }

        // Extract text content
        String content = extractText(file);

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Could not extract text from the document");
        }

        // Save document
        Document document = new Document();
        document.setFilename(file.getOriginalFilename());
        document.setFileType(contentType);
        document.setFileSize(file.getSize());
        document.setContent(content);
        document.setUploadedAt(LocalDateTime.now());

        document = documentRepository.save(document);

        log.info("Document uploaded successfully: {}", document.getFilename());

        return new DocumentUploadResponse(
                document.getId(),
                document.getFilename(),
                document.getFileType(),
                document.getFileSize(),
                "Document uploaded successfully",
                document.getUploadedAt()
        );
    }

    /**
     * Extract text from different file types
     */
    private String extractText(MultipartFile file) throws IOException {
        String contentType = file.getContentType();

        if (contentType == null) {
            throw new IllegalArgumentException("Content type is null");
        }

        return switch (contentType) {
            case "application/pdf" -> extractFromPdf(file.getInputStream());
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ->
                    extractFromDocx(file.getInputStream());
            case "text/plain" -> new String(file.getBytes(), StandardCharsets.UTF_8);
            default -> throw new IllegalArgumentException("Unsupported file type");
        };
    }

    /**
     * Extract text from PDF
     */
    private String extractFromPdf(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.debug("Extracted {} characters from PDF", text.length());
            return text;
        }
    }

    /**
     * Extract text from DOCX
     */
    private String extractFromDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            String text = extractor.getText();
            log.debug("Extracted {} characters from DOCX", text.length());
            return text;
        }
    }

    /**
     * Get all documents
     */
    public List<DocumentResponse> getAllDocuments() {
        log.debug("Fetching all documents");
        return documentRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific document
     */
    public DocumentResponse getDocument(Long id) {
        log.debug("Fetching document with id: {}", id);
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
        return convertToResponse(document);
    }

    /**
     * Generate AI summary for a document
     */
    @Transactional
    public String generateSummary(Long id) {
        log.info("Generating summary for document id: {}", id);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));

        // Return existing summary if already generated
        if (document.getSummary() != null && !document.getSummary().isEmpty()) {
            log.debug("Returning existing summary for document id: {}", id);
            return document.getSummary();
        }

        String promptText = """
                Please provide a comprehensive summary of the following document.
                The summary should be concise yet informative, highlighting the main points and key takeaways.
                
                Document Content:
                {content}
                
                Provide the summary in a clear, structured format.
                """;

        PromptTemplate promptTemplate = new PromptTemplate(promptText);
        Prompt prompt = promptTemplate.create(Map.of("content", truncateContent(document.getContent(), 4000)));

        log.debug("Calling AI to generate summary");
        String summary = chatClient.prompt(prompt).call().content();

        document.setSummary(summary);
        documentRepository.save(document);

        log.info("Summary generated and saved for document id: {}", id);
        return summary;
    }

    /**
     * Ask a question about a document
     */
    @Transactional
    public QuestionAnswerResponse askQuestion(Long id, String question) {
        log.info("Processing question for document id: {}", id);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));

        String promptText = """
                Based on the following document, please answer the question accurately and concisely.
                If the answer cannot be found in the document, please state that clearly.
                
                Document Content:
                {content}
                
                Question: {question}
                
                Answer:
                """;

        PromptTemplate promptTemplate = new PromptTemplate(promptText);
        Prompt prompt = promptTemplate.create(Map.of(
                "content", truncateContent(document.getContent(), 4000),
                "question", question
        ));

        log.debug("Calling AI to answer question");
        String answer = chatClient.prompt(prompt).call().content();

        QuestionAnswer qa = new QuestionAnswer();
        qa.setDocument(document);
        qa.setQuestion(question);
        qa.setAnswer(answer);
        qa.setAskedAt(LocalDateTime.now());

        qa = questionAnswerRepository.save(qa);

        log.info("Question answered and saved for document id: {}", id);

        return new QuestionAnswerResponse(
                qa.getId(),
                qa.getQuestion(),
                qa.getAnswer(),
                qa.getAskedAt()
        );
    }

    /**
     * Get question history for a document
     */
    public List<QuestionAnswerResponse> getQuestionHistory(Long id) {
        log.debug("Fetching question history for document id: {}", id);

        if (!documentRepository.existsById(id)) {
            throw new RuntimeException("Document not found with id: " + id);
        }

        return questionAnswerRepository.findByDocumentIdOrderByAskedAtDesc(id).stream()
                .map(qa -> new QuestionAnswerResponse(
                        qa.getId(),
                        qa.getQuestion(),
                        qa.getAnswer(),
                        qa.getAskedAt()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Delete a document
     */
    @Transactional
    public void deleteDocument(Long id) {
        log.info("Deleting document with id: {}", id);

        if (!documentRepository.existsById(id)) {
            throw new RuntimeException("Document not found with id: " + id);
        }
        documentRepository.deleteById(id);

        log.info("Document deleted successfully with id: {}", id);
    }

    /**
     * Convert Document entity to DocumentResponse DTO
     */
    private DocumentResponse convertToResponse(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getFilename(),
                document.getFileType(),
                document.getFileSize(),
                document.getSummary(),
                document.getUploadedAt()
        );
    }

    /**
     * Truncate content to avoid token limits
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        return content.length() > maxLength ? content.substring(0, maxLength) + "..." : content;
    }
}
