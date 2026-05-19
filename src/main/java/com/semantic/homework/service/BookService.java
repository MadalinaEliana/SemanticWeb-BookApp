package com.semantic.homework.service;

import com.semantic.homework.dto.BookDto;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookService {

    private static final String NS = "http://example.org/book-recommendation/";
    private static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
    private static final String BOOK_TYPE = NS + "Book";

    private final RdfService rdfService;

    public BookService(RdfService rdfService) {
        this.rdfService = rdfService;
    }

    public List<BookDto> listAllBooks() {
        Model model = rdfService.getModel();
        String sparql = """
            PREFIX ex: <http://example.org/book-recommendation/>
            PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            SELECT ?book ?title ?author ?level ?description WHERE {
                ?book rdf:type ex:Book .
                ?book ex:title ?title .
                OPTIONAL { ?book ex:author ?author . }
                OPTIONAL { ?book ex:readingLevel ?level . }
                OPTIONAL { ?book ex:description ?description . }
            }
            ORDER BY ?title
            """;

        List<BookDto> books = new ArrayList<>();
        try (QueryExecution qe = QueryExecutionFactory.create(QueryFactory.create(sparql), model)) {
            ResultSet rs = qe.execSelect();
            while (rs.hasNext()) {
                QuerySolution sol = rs.nextSolution();
                String bookUri = sol.getResource("book").getURI();
                String id = localName(bookUri);
                String title = sol.getLiteral("title").getString();
                String author = sol.contains("author") ? sol.getLiteral("author").getString() : "";
                String level = sol.contains("level") ? sol.getLiteral("level").getString() : "";
                String desc = sol.contains("description") ? sol.getLiteral("description").getString() : "";
                List<String> genres = getGenresForBook(model, bookUri);
                books.add(new BookDto(id, bookUri, title, author, genres, level, desc));
            }
        }
        return books;
    }

    public BookDto getBook(String id) {
        String uri = NS + "book/" + id;
        Model model = rdfService.getModel();
        Resource res = model.getResource(uri);

        String title = getLiteral(model, res, NS + "title");
        String author = getLiteral(model, res, NS + "author");
        String level = getLiteral(model, res, NS + "readingLevel");
        String desc = getLiteral(model, res, NS + "description");
        List<String> genres = getGenresForBook(model, uri);

        if (title == null) return null;
        return new BookDto(id, uri, title, author, genres, level, desc);
    }

    public void addOrUpdateBook(String title, List<String> genres, String readingLevel, String author, String description) {
        Model model = rdfService.getModel();
        String id = slugify(title);
        String uri = NS + "book/" + id;
        Resource book = model.createResource(uri);
        Property rdfType = model.createProperty(RDF_TYPE);
        Property titleProp = model.createProperty(NS + "title");
        Property authorProp = model.createProperty(NS + "author");
        Property levelProp = model.createProperty(NS + "readingLevel");
        Property descProp = model.createProperty(NS + "description");
        Property genreProp = model.createProperty(NS + "genre");

        model.removeAll(book, titleProp, null);
        model.removeAll(book, authorProp, null);
        model.removeAll(book, levelProp, null);
        model.removeAll(book, descProp, null);
        model.removeAll(book, genreProp, null);

        book.addProperty(rdfType, model.createResource(BOOK_TYPE));
        book.addProperty(titleProp, title);
        if (author != null && !author.isBlank()) book.addProperty(authorProp, author);
        if (readingLevel != null && !readingLevel.isBlank()) book.addProperty(levelProp, readingLevel);
        if (description != null && !description.isBlank()) book.addProperty(descProp, description);

        for (String genre : genres) {
            String genreUri = NS + "theme/" + slugify(genre);
            Resource genreRes = model.createResource(genreUri);
            if (!model.containsResource(genreRes)) {
                genreRes.addProperty(model.createProperty(RDF_TYPE), model.createResource(NS + "Theme"));
                genreRes.addProperty(model.createProperty(NS + "name"), genre.trim());
            }
            book.addProperty(genreProp, genreRes);
        }
    }

    public boolean changeReadingLevel(String title, String newLevel) {
        Model model = rdfService.getModel();
        String sparql = """
            PREFIX ex: <http://example.org/book-recommendation/>
            SELECT ?book WHERE {
                ?book ex:title "%s" .
            }
            """.formatted(title.replace("\"", "\\\""));

        String foundUri = null;
        try (QueryExecution qe = QueryExecutionFactory.create(QueryFactory.create(sparql), model)) {
            ResultSet rs = qe.execSelect();
            if (rs.hasNext()) {
                foundUri = rs.nextSolution().getResource("book").getURI();
            }
        }
        if (foundUri == null) return false;

        Resource book = model.getResource(foundUri);
        Property levelProp = model.createProperty(NS + "readingLevel");
        model.removeAll(book, levelProp, null);
        book.addProperty(levelProp, newLevel);
        return true;
    }

    private List<String> getGenresForBook(Model model, String bookUri) {
        List<String> genres = new ArrayList<>();
        Resource book = model.getResource(bookUri);
        Property genreProp = model.createProperty(NS + "genre");
        Property nameProp = model.createProperty(NS + "name");
        StmtIterator it = book.listProperties(genreProp);
        while (it.hasNext()) {
            RDFNode obj = it.next().getObject();
            if (obj.isResource()) {
                String name = getLiteral(model, obj.asResource(), NS + "name");
                genres.add(name != null ? name : localName(obj.asResource().getURI()));
            }
        }
        return genres;
    }

    private String getLiteral(Model model, Resource res, String propUri) {
        Property prop = model.createProperty(propUri);
        Statement stmt = res.getProperty(prop);
        return stmt != null ? stmt.getString() : null;
    }

    private String localName(String uri) {
        int h = uri.lastIndexOf('#');
        int s = uri.lastIndexOf('/');
        int idx = Math.max(h, s);
        return idx >= 0 ? uri.substring(idx + 1) : uri;
    }

    private String slugify(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}
