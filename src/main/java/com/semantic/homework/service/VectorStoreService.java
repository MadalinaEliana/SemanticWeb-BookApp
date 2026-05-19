package com.semantic.homework.service;

import com.semantic.homework.dto.BookDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VectorStoreService {

    private record BookVector(BookDto book, float[] embedding) {}

    private final List<BookVector> store = new ArrayList<>();

    public void add(BookDto book, float[] embedding) {
        store.add(new BookVector(book, embedding));
    }

    public boolean isEmpty() {
        return store.isEmpty();
    }

    public List<BookDto> findSimilar(float[] queryEmbedding, int k) {
        return store.stream()
                .sorted(Comparator.comparingDouble(bv -> -cosineSimilarity(queryEmbedding, bv.embedding())))
                .limit(k)
                .map(BookVector::book)
                .collect(Collectors.toList());
    }

    private float cosineSimilarity(float[] a, float[] b) {
        float dot = 0, normA = 0, normB = 0;
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0f;
        return dot / (float) (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
