package com.semantic.homework.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BookController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Book Recommendation System");
        return "index";
    }

    @GetMapping("/books")
    public String listBooks(Model model) {
        return "books";
    }

    @GetMapping("/visualize")
    public String visualizeRdf(Model model) {
        return "visualize";
    }

}
