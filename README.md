# Semantic Web – Book Recommendation System

**GitHub repository:** https://github.com/MadalinaEliana/SemanticWeb-BookApp

## Team members

- BACCELA Radu-Costin
- ȘTEFAN Mădălina-Eliana

---

## Contributions

### ȘTEFAN Mădălina-Eliana
- Exercise 1: Wrote the RDF/XML for the book recommendation scenario (users, books, themes)
- Exercise 2: Added the RDF graph visualization feature (file upload + SVG rendering using JUNG)
- Exercise 3: Implemented add/modify book functionality using Jena API and SPARQL
- Exercise 4: Implemented the book listing page and individual book detail pages using Jena API

### BACCELA Radu-Costin
- Exercise 5: Created the OWL ontology for the book recommendation system using Protégé, visualized using OWLViz
- Exercise 6: Wrote 5 SPARQL queries for the ontology, executed via a Python script using rdflib
- Exercise 7: Built the AI chatbot with a floating chat widget on all pages, context-aware conversation starters, RAG-based responses using a vector database (Gemini embeddings + cosine similarity), and book search by author and theme (Groq LLM / Llama 3.1)

---

## How to run

```
cd SemanticWeb-BookApp
mvn clean spring-boot:run
```

App runs at **http://localhost:8080**

Requires API keys in `src/main/resources/application.properties`:
- `gemini.api.key` – from [aistudio.google.com/apikey](https://aistudio.google.com/apikey)
- `groq.api.key` – from [console.groq.com/keys](https://console.groq.com/keys)
