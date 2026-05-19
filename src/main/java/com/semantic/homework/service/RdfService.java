package com.semantic.homework.service;

import com.semantic.homework.dto.GraphEdge;
import com.semantic.homework.dto.GraphNode;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Dimension;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RdfService {

    private Model model;

    public RdfService() {
        this.model = ModelFactory.createDefaultModel();
    }

    public void loadRdfFromString(String rdfContent) {
        model = ModelFactory.createDefaultModel();
        InputStream inputStream = new ByteArrayInputStream(rdfContent.getBytes(StandardCharsets.UTF_8));
        model.read(inputStream, null, "RDF/XML");
    }

    public void loadRdfFromMultipartFile(MultipartFile file) throws IOException {
        model = ModelFactory.createDefaultModel();
        try (InputStream inputStream = file.getInputStream()) {
            model.read(inputStream, null, "RDF/XML");
        }
    }

    public void loadRdfFromFile(String filePath) {
        model = ModelFactory.createDefaultModel();
        model.read(filePath);
    }

    public List<Resource> getResourcesByType(String typeUri) {
        List<Resource> resources = new ArrayList<>();
        ResIterator resIterator = model.listResourcesWithProperty(
            model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
            model.getResource(typeUri)
        );
        
        while (resIterator.hasNext()) {
            resources.add(resIterator.nextResource());
        }
        return resources;
    }

    public List<Resource> getAllResources() {
        List<Resource> resources = new ArrayList<>();
        ResIterator resIterator = model.listSubjects();
        while (resIterator.hasNext()) {
            resources.add(resIterator.nextResource());
        }
        return resources;
    }

    public Model getModel() {
        return model;
    }

    public String exportAsRdfXml() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        model.write(outputStream, "RDF/XML");
        return outputStream.toString(StandardCharsets.UTF_8);
    }

    public void clear() {
        model = ModelFactory.createDefaultModel();
    }

    public List<GraphNode> buildGraphNodes() {
        GraphData graphData = buildGraphData();
        return graphData.nodes();
    }

    public List<GraphEdge> buildGraphEdges() {
        GraphData graphData = buildGraphData();
        return graphData.edges();
    }

    public GraphData buildGraphData() {
        Graph<String, String> graph = new SparseMultigraph<>();
        Map<String, String> vertexLabels = new LinkedHashMap<>();
        Map<String, String> vertexTypes = new LinkedHashMap<>();
        Map<String, String> edgeLabels = new LinkedHashMap<>();
        Map<String, Integer> edgeSourceX = new LinkedHashMap<>();
        Map<String, Integer> edgeSourceY = new LinkedHashMap<>();
        Map<String, Integer> edgeTargetX = new LinkedHashMap<>();
        Map<String, Integer> edgeTargetY = new LinkedHashMap<>();

        int edgeCounter = 1;
        int literalCounter = 1;

        for (Statement statement : model.listStatements().toList()) {
            String subjectId = resourceId(statement.getSubject().toString());
            String subjectLabel = labelForResource(statement.getSubject());
            addVertex(graph, vertexLabels, vertexTypes, subjectId, subjectLabel, "resource");

            String objectId;
            String objectLabel;
            String objectType;
            if (statement.getObject().isResource()) {
                Resource resource = statement.getObject().asResource();
                objectId = resourceId(resource.toString());
                objectLabel = labelForResource(resource);
                objectType = "resource";
            } else {
                objectId = "literal:" + literalCounter++;
                objectLabel = statement.getObject().asLiteral().getString();
                objectType = "literal";
            }

            addVertex(graph, vertexLabels, vertexTypes, objectId, objectLabel, objectType);

            String edgeId = "edge-" + edgeCounter++;
            graph.addEdge(edgeId, subjectId, objectId);
            edgeLabels.put(edgeId, labelForProperty(statement.getPredicate()));
        }

        CircleLayout<String, String> layout = new CircleLayout<>(graph);
        layout.setSize(new Dimension(1100, 700));
        layout.initialize();

        List<GraphNode> nodes = vertexLabels.entrySet().stream()
            .map(entry -> new GraphNode(
                entry.getKey(),
                entry.getValue(),
                vertexTypes.getOrDefault(entry.getKey(), "resource"),
                (int) layout.getX(entry.getKey()),
                (int) layout.getY(entry.getKey())
            ))
            .collect(Collectors.toList());

        List<GraphEdge> edges = graph.getEdges().stream()
            .map(edgeId -> {
                var endpoints = graph.getEndpoints(edgeId);
                String sourceId = endpoints.getFirst();
                String targetId = endpoints.getSecond();
                int sourceX = (int) layout.getX(sourceId);
                int sourceY = (int) layout.getY(sourceId);
                int targetX = (int) layout.getX(targetId);
                int targetY = (int) layout.getY(targetId);
                return new GraphEdge(edgeId, sourceId, targetId, edgeLabels.get(edgeId), sourceX, sourceY, targetX, targetY);
            })
            .collect(Collectors.toList());

        return new GraphData(nodes, edges);
    }

    private void addVertex(Graph<String, String> graph, Map<String, String> vertexLabels, Map<String, String> vertexTypes, String id, String label, String type) {
        graph.addVertex(id);
        vertexLabels.putIfAbsent(id, label);
        vertexTypes.putIfAbsent(id, type);
    }

    private String resourceId(String value) {
        return "resource:" + value;
    }

    private String labelForResource(Resource resource) {
        String localName = resource.getLocalName();
        if (localName != null && !localName.isBlank()) {
            return localName;
        }
        return resource.getURI();
    }

    private String labelForProperty(Property property) {
        String localName = property.getLocalName();
        if (localName != null && !localName.isBlank()) {
            return localName;
        }
        return property.getURI();
    }

    public record GraphData(List<GraphNode> nodes, List<GraphEdge> edges) {
    }

}
