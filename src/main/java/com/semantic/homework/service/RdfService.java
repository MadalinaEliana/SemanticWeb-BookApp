package com.semantic.homework.service;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class RdfService {

    private Model model;

    public RdfService() {
        this.model = ModelFactory.createDefaultModel();
    }

    /**
     * Load RDF from XML string
     */
    public void loadRdfFromString(String rdfContent) {
        model = ModelFactory.createDefaultModel();
        InputStream inputStream = new ByteArrayInputStream(rdfContent.getBytes());
        model.read(inputStream, null, "RDF/XML");
    }

    /**
     * Load RDF from file
     */
    public void loadRdfFromFile(String filePath) {
        model = ModelFactory.createDefaultModel();
        model.read(filePath);
    }

    /**
     * Get all resources of a specific type
     */
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

    /**
     * Get all resources (subjects)
     */
    public List<Resource> getAllResources() {
        List<Resource> resources = new ArrayList<>();
        ResIterator resIterator = model.listSubjects();
        while (resIterator.hasNext()) {
            resources.add(resIterator.nextResource());
        }
        return resources;
    }

    /**
     * Get model
     */
    public Model getModel() {
        return model;
    }

    /**
     * Export model as RDF/XML string
     */
    public String exportAsRdfXml() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        model.write(outputStream, "RDF/XML");
        return outputStream.toString();
    }

    /**
     * Clear model
     */
    public void clear() {
        model = ModelFactory.createDefaultModel();
    }

}
