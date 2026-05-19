package com.semantic.homework.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class GeminiService {

    private static final String EMBED_BASE = "https://generativelanguage.googleapis.com/v1beta/models";
    private static final String CHAT_BASE  = "https://generativelanguage.googleapis.com/v1beta/models";
    private static final String EMBED_MODEL = "gemini-embedding-001";
    private static final String CHAT_MODEL  = "gemini-2.0-flash";

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.equals("YOUR_GEMINI_API_KEY_HERE");
    }

    public float[] getEmbedding(String text) throws Exception {
        String url = EMBED_BASE + "/" + EMBED_MODEL + ":embedContent?key=" + apiKey;

        JsonObject part = new JsonObject();
        part.addProperty("text", text);
        JsonArray parts = new JsonArray();
        parts.add(part);
        JsonObject content = new JsonObject();
        content.add("parts", parts);

        JsonObject body = new JsonObject();
        body.add("content", content);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject json = gson.fromJson(response.body(), JsonObject.class);

        if (!json.has("embedding")) {
            throw new RuntimeException("Gemini embedding API error: " + response.body());
        }

        JsonArray values = json.getAsJsonObject("embedding").getAsJsonArray("values");
        float[] embedding = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            embedding[i] = values.get(i).getAsFloat();
        }
        return embedding;
    }

    public String chat(String systemPrompt, String userMessage) throws Exception {
        String url = CHAT_BASE + "/" + CHAT_MODEL + ":generateContent?key=" + apiKey;

        JsonObject sysPart = new JsonObject();
        sysPart.addProperty("text", systemPrompt);
        JsonArray sysParts = new JsonArray();
        sysParts.add(sysPart);
        JsonObject sysInstruction = new JsonObject();
        sysInstruction.add("parts", sysParts);

        JsonObject userPart = new JsonObject();
        userPart.addProperty("text", userMessage);
        JsonArray userParts = new JsonArray();
        userParts.add(userPart);
        JsonObject userContent = new JsonObject();
        userContent.addProperty("role", "user");
        userContent.add("parts", userParts);
        JsonArray contents = new JsonArray();
        contents.add(userContent);

        JsonObject body = new JsonObject();
        body.add("system_instruction", sysInstruction);
        body.add("contents", contents);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject json = gson.fromJson(response.body(), JsonObject.class);

        if (json.has("error")) {
            throw new RuntimeException("Gemini chat API error: " + json.get("error").toString());
        }
        if (!json.has("candidates")) {
            throw new RuntimeException("Unexpected Gemini response: " + response.body());
        }

        return json.getAsJsonArray("candidates")
                .get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString();
    }
}
