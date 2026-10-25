package com.SpringAi.SpringAi2.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VectorStoreService {

    private static class ChunkEntry {

        String text;
        List<Double> embedding;

        ChunkEntry(String text, List<Double> embedding) {
            this.text = text;
            this.embedding = embedding;
        }

    }

    private final List<ChunkEntry> store = new ArrayList<>();

    public void clear() {
        store.clear();
    }

    public void add(String text, List<Double> embedding) {
        store.add(new ChunkEntry(text, embedding));
    }

    public List<String> search(
            List<Double> queryEmbedding,
            int topK,
            EmbeddingService embeddingService) {

        List<double[]> scored = new ArrayList<>();
        for (int i = 0; i < store.size(); i++) {
            double similarity = embeddingService.cosineSimilarity(
                    queryEmbedding,
                    store.get(i).embedding
            );
            scored.add(new double[]{i, similarity});
        }

        scored.sort((a, b) -> Double.compare(b[1], a[1]));
        List<String> results = new ArrayList<>();

        for (int i = 0; i < Math.min(topK, scored.size()); i++) {
            int index = (int) scored.get(i)[0];
            results.add(store.get(index).text);
        }

        return results;

    }
}
