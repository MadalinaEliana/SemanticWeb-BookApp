package com.semantic.homework.service;

import com.semantic.homework.dto.BookDto;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookEmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(BookEmbeddingService.class);

    @Autowired
    private BookService bookService;
    @Autowired
    private GeminiService geminiService;
    @Autowired
    private VectorStoreService vectorStoreService;

    @PostConstruct
    public void initialize() {
        if (!geminiService.isConfigured()) {
            log.warn("Gemini API key not set — chatbot vector store skipped.");
            return;
        }
        try {
            List<BookDto> books = bookService.listAllBooks();
            for (BookDto book : books) {
                String text = buildBookText(book);
                float[] embedding = geminiService.getEmbedding(text);
                vectorStoreService.add(book, embedding);
                log.info("Embedded: {}", book.title());
                Thread.sleep(500);
            }
            log.info("Vector store ready with {} books.", books.size());
        } catch (Exception e) {
            log.error("Failed to initialize embeddings: {}", e.getMessage());
        }
    }

    public static String buildBookText(BookDto book) {
        StringBuilder sb = new StringBuilder();
        String title = book.title() != null ? book.title() : book.id();
        sb.append("Book: ").append(title);
        if (book.author() != null && !book.author().isBlank())
            sb.append(" by ").append(book.author());
        if (book.genres() != null && !book.genres().isEmpty())
            sb.append(". Genre: ").append(String.join(", ", book.genres()));
        if (book.readingLevel() != null && !book.readingLevel().isBlank())
            sb.append(". Reading level: ").append(book.readingLevel());
        if (book.description() != null && !book.description().isBlank())
            sb.append(". ").append(book.description());
        return sb.toString();
    }
}
