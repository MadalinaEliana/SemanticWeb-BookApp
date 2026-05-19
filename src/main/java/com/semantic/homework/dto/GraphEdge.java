package com.semantic.homework.dto;

public record GraphEdge(String id, String sourceId, String targetId, String label, int sourceX, int sourceY, int targetX, int targetY) {

    public GraphEdge(String id, String sourceId, String targetId, String label) {
        this(id, sourceId, targetId, label, 0, 0, 0, 0);
    }
}