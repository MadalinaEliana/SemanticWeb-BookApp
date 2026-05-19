package com.semantic.homework.controller;

import com.semantic.homework.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/starters")
    public ResponseEntity<?> getStarters(
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String bookTitle,
            @RequestParam(required = false) String bookAuthor,
            @RequestParam(required = false) String bookGenre) {
        List<String> starters = chatService.getStarters(page, bookTitle, bookAuthor, bookGenre);
        return ResponseEntity.ok(Map.of("starters", starters));
    }

    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, String> request) {
        try {
            String response = chatService.chat(
                    request.get("message"),
                    request.get("page"),
                    request.get("bookTitle"),
                    request.get("bookAuthor")
            );
            return ResponseEntity.ok(Map.of("response", response));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("response", "Error: " + e.getMessage()));
        }
    }
}
