package com.semantic.homework.controller;

import com.semantic.homework.dto.BookDto;
import com.semantic.homework.service.BookService;
import com.semantic.homework.service.RdfService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
public class BookController {

    private final RdfService rdfService;
    private final BookService bookService;

    public BookController(RdfService rdfService, BookService bookService) {
        this.rdfService = rdfService;
        this.bookService = bookService;
        try {
            var stream = getClass().getResourceAsStream("/data/books.rdf");
            if (stream != null) {
                rdfService.getModel().read(stream, null, "RDF/XML");
            }
        } catch (Exception ignored) {}
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Book Recommendation System");
        return "index";
    }

    @GetMapping("/books")
    public String listBooks(Model model) {
        List<BookDto> books = bookService.listAllBooks();
        model.addAttribute("books", books);
        return "books";
    }

    @GetMapping("/books/{id}")
    public String bookDetail(@PathVariable String id, Model model) {
        BookDto book = bookService.getBook(id);
        if (book == null) {
            model.addAttribute("errorMessage", "Book not found: " + id);
            model.addAttribute("books", bookService.listAllBooks());
            return "books";
        }
        model.addAttribute("book", book);
        return "book-detail";
    }

    @GetMapping("/books/edit")
    public String editBookForm(@RequestParam(required = false) String id, Model model) {
        if (id != null) {
            BookDto book = bookService.getBook(id);
            model.addAttribute("book", book);
        }
        return "book-edit";
    }

    @PostMapping("/books/save")
    public String saveBook(@RequestParam String title,
                           @RequestParam(defaultValue = "") String genres,
                           @RequestParam String readingLevel,
                           @RequestParam(defaultValue = "") String author,
                           @RequestParam(defaultValue = "") String description,
                           Model model) {
        List<String> genreList = List.of(genres.split(",")).stream()
                .map(String::trim).filter(s -> !s.isBlank()).toList();
        bookService.addOrUpdateBook(title, genreList, readingLevel, author, description);
        return "redirect:/books";
    }

    @GetMapping("/visualize")
    public String visualizeRdf(Model model) {
        model.addAttribute("hasGraph", false);
        return "visualize";
    }

    @PostMapping(path = "/visualize/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadRdf(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("hasGraph", false);
            model.addAttribute("errorMessage", "Please choose an RDF/XML file to upload.");
            return "visualize";
        }
        try {
            rdfService.loadRdfFromMultipartFile(file);
            RdfService.GraphData graphData = rdfService.buildGraphData();
            model.addAttribute("hasGraph", true);
            model.addAttribute("fileName", file.getOriginalFilename());
            model.addAttribute("graphNodes", graphData.nodes());
            model.addAttribute("graphEdges", graphData.edges());
            model.addAttribute("nodeCount", graphData.nodes().size());
            model.addAttribute("edgeCount", graphData.edges().size());
            model.addAttribute("message", "RDF file loaded successfully.");
        } catch (IOException | RuntimeException e) {
            model.addAttribute("hasGraph", false);
            model.addAttribute("errorMessage", "Could not read the RDF/XML file: " + e.getMessage());
        }
        return "visualize";
    }
}