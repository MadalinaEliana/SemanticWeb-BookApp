from rdflib import Graph

TTL_PATH = r"c:\Users\Radu\Desktop\An 4\Semestrul 2\SeWeb\Project hw2\sparql_queries\SemanticWeb-BookAppTurtle.ttl"

g = Graph()
g.parse(TTL_PATH)
print(f"Loaded {len(g)} triples from ontology.\n")

PREFIX = """
PREFIX ontology: <http://example.org/book-recommendation/ontology#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
"""

queries = [
    (
        "Query 1 - List all books with author and reading level",
        PREFIX + """
        SELECT ?title ?author ?level WHERE {
            ?book rdf:type ontology:Book .
            OPTIONAL { ?book ontology:hasTitle ?title . }
            OPTIONAL { ?book ontology:hasName ?title . }
            ?book ontology:hasAuthor ?author .
            ?book ontology:hasReadingLevel ?level .
        } ORDER BY ?title
        """
    ),
    (
        "Query 2 - List all users with their preferred themes",
        PREFIX + """
        SELECT ?userName ?themeName WHERE {
            ?user rdf:type ontology:User .
            ?user ontology:hasName ?userName .
            ?user ontology:hasPreferredTheme ?theme .
            ?theme ontology:hasName ?themeName .
        }
        """
    ),
    (
        "Query 3 - List all books with their genres",
        PREFIX + """
        SELECT ?title ?genre WHERE {
            ?book rdf:type ontology:Book .
            OPTIONAL { ?book ontology:hasTitle ?title . }
            OPTIONAL { ?book ontology:hasName ?title . }
            ?book ontology:hasGenre ?theme .
            ?theme ontology:hasName ?genre .
        } ORDER BY ?title
        """
    ),
    (
        "Query 4 - Find all books suitable for Beginner readers",
        PREFIX + """
        SELECT ?title ?author WHERE {
            ?book rdf:type ontology:Book .
            OPTIONAL { ?book ontology:hasTitle ?title . }
            OPTIONAL { ?book ontology:hasName ?title . }
            ?book ontology:hasAuthor ?author .
            ?book ontology:hasReadingLevel "Beginner" .
        }
        """
    ),
    (
        "Query 5 - Find books matching Alice's preferred theme (recommendations)",
        PREFIX + """
        SELECT ?userName ?bookTitle ?themeName WHERE {
            ?user rdf:type ontology:User .
            ?user ontology:hasName ?userName .
            ?user ontology:hasPreferredTheme ?theme .
            ?theme ontology:hasName ?themeName .
            ?book rdf:type ontology:Book .
            OPTIONAL { ?book ontology:hasTitle ?bookTitle . }
            OPTIONAL { ?book ontology:hasName ?bookTitle . }
            ?book ontology:hasGenre ?theme .
            FILTER(?userName = "Alice")
        }
        """
    ),
]

for title, query in queries:
    print("=" * 65)
    print(title)
    print("=" * 65)
    results = list(g.query(query))
    if results:
        vars_ = [str(v) for v in results[0].asdict().keys()]
        col_width = 25
        print(" | ".join(v.ljust(col_width) for v in vars_))
        print("-" * (col_width * len(vars_) + 3 * (len(vars_) - 1)))
        seen = set()
        for row in results:
            values = tuple(str(v) for v in row)
            if values not in seen:
                seen.add(values)
                print(" | ".join(str(v).ljust(col_width) for v in row))
    else:
        print("No results found.")
    print()
