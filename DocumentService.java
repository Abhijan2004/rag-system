package com.SpringAi.SpringAi2.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.SpringAi.SpringAi2.model.AnswerResponse;

import java.util.List;

public class DocumentService {

    @Autowired
    private ChunkingService chunkingService;
    @Autowired
    private EmbeddingService embeddingService;
    @Autowired
    private VectorStoreService vectorStore;

    public void indexDocument(String documentText) throws Exception {

        vectorStore.clear();

        List<String> chunks = chunkingService.chunkDocument(
                documentText, 500, 50);


        for (String chunk : chunks) {
            List<Double> embedding =
                    embeddingService.getEmbedding(chunk);
            vectorStore.add(chunk, embedding);
        }
    }

    public AnswerResponse answerQuestion(
            String question) throws Exception {

        // Step 1: Embed the question
        List<Double> questionEmbedding =
                embeddingService.getEmbedding(question);


        return null;
    }
}
