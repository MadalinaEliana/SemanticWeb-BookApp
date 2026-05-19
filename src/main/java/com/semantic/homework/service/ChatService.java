package com.semantic.homework.service;

import com.semantic.homework.dto.BookDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    @Autowired
    private GeminiService geminiService;
    @Autowired
    private GroqService groqService;
    @Autowired
    private VectorStoreService vectorStoreService;

    public List<String> getStarters(String page, String bookTitle, String bookAuthor, String bookGenre) {
        return switch (page != null ? page : "") {
            case "book-detail" -> List.of(
                    "Tell me more about \"" + bookTitle + "\"",
                    "What other books are similar to \"" + bookTitle + "\"?",
                    "What else has " + bookAuthor + " written?"
            );
            case "books" -> List.of(
                    "What book am I most likely to enjoy from this list?",
                    "Which book is best for beginners?",
                    "Can you recommend a Science Fiction book?"
            );
            default -> List.of(
                    "What books do you have available?",
                    "Can you recommend a book for me?",
                    "What is a good book for an intermediate reader?"
            );
        };
    }

    public String chat(String userMessage, String page, String bookTitle, String bookAuthor) throws Exception {
        if (!groqService.isConfigured()) {
            return "The chatbot is not configured. Please set the Groq API key in application.properties.";
        }

        float[] queryEmbedding = geminiService.getEmbedding(userMessage);
        List<BookDto> relevantBooks = vectorStoreService.findSimilar(queryEmbedding, 3);

        StringBuilder systemPrompt = new StringBuilder();
        systemPrompt.append("You are a helpful book assistant. ");
        systemPrompt.append("Answer ONLY based on the book data provided below. ");
        systemPrompt.append("Do NOT use your own knowledge about books — if a book is not in the database, say so. ");
        systemPrompt.append("Keep answers concise and friendly.\n\n");

        if (!relevantBooks.isEmpty()) {
            systemPrompt.append("BOOKS IN DATABASE:\n");
            for (BookDto book : relevantBooks) {
                systemPrompt.append("- ").append(BookEmbeddingService.buildBookText(book)).append("\n");
            }
        }

        if ("book-detail".equals(page) && bookTitle != null) {
            systemPrompt.append("\nThe user is currently viewing: \"").append(bookTitle).append("\"");
            if (bookAuthor != null) systemPrompt.append(" by ").append(bookAuthor);
            systemPrompt.append(".\n");
        }

        return groqService.chat(systemPrompt.toString(), userMessage);
    }
}
