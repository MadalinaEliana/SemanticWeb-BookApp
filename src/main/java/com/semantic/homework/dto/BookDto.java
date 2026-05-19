package com.semantic.homework.dto;

import java.util.List;

public record BookDto(
        String id,
        String uri,
        String title,
        String author,
        List<String> genres,
        String readingLevel,
        String description
) {}
