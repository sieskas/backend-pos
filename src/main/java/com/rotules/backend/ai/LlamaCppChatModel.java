package com.rotules.backend.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class LlamaCppChatModel implements ChatLanguageModel {
    private final String serverUrl;
    private final HttpClient http;

    public LlamaCppChatModel(String serverUrl) {
        this.serverUrl = serverUrl;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages) {
        try {
            // Construire les messages JSON
            StringBuilder messagesJson = new StringBuilder("[");
            for (ChatMessage message : messages) {
                String role;
                String content;
                if (message instanceof UserMessage) {
                    role = "user";
                    content = ((UserMessage) message).singleText();
                } else if (message instanceof AiMessage) {
                    role = "assistant";
                    content = ((AiMessage) message).text();
                } else if (message instanceof SystemMessage) {
                    role = "system";
                    content = ((SystemMessage) message).text();
                } else {
                    continue;
                }
                messagesJson.append(String.format("{\"role\":\"%s\",\"content\":\"%s\"},", role, content.replace("\"", "\\\"")));
            }

            // Retirer la dernière virgule
            if (messagesJson.charAt(messagesJson.length() - 1) == ',') {
                messagesJson.setLength(messagesJson.length() - 1);
            }
            messagesJson.append("]");

            // Construire le JSON final
            String jsonBody = """
                    {
                      "model": "llama3",
                      "messages": %s,
                      "temperature": 0.7,
                      "stream": false
                    }
                    """.formatted(messagesJson.toString());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            // Parser la réponse (très simple, mieux d’utiliser Jackson ou Gson en vrai)
            String body = response.body();
            String content = body.replaceAll(".*\"content\"\\s*:\\s*\"(.*?)\".*", "$1");

            return Response.from(new AiMessage(content));
        } catch (Exception e) {
            return Response.from(new AiMessage("Erreur d'appel à llama-server: " + e.getMessage()));
        }
    }
}