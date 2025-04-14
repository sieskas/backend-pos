package com.rotules.backend.ai;

import dev.langchain4j.agent.tool.Tool;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DataFetcherTool {

    @Tool("Récupère les données de ventes depuis un backend pour une période donnée")
    public static String getVentesParDate(String startDate, String endDate) {
        // Appel REST à ton backend Java
        HttpClient client = HttpClient.newHttpClient();
        String url = "http://localhost:8081/api/v1/data?startDate=" + startDate + "&endDate=" + endDate;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body(); // JSON ou texte à injecter dans le prompt
        } catch (Exception e) {
            return "Erreur lors de l'appel backend: " + e.getMessage();
        }
    }
}