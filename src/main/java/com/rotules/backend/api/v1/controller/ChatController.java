package com.rotules.backend.api.v1.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rotules.backend.api.v1.controller.resources.ChatMessageDTO;
import com.rotules.backend.api.v1.controller.resources.ChatMessageResponseDTO;
import com.rotules.backend.domain.ChatMessage;
import com.rotules.backend.domain.ChatSession;
import com.rotules.backend.domain.SenderTypeEnum;
import com.rotules.backend.domain.User;
import com.rotules.backend.outcall.db.repository.ChatMessageRepository;
import com.rotules.backend.outcall.db.repository.ChatSessionRepository;
import com.rotules.backend.outcall.db.repository.UserRepository;
import com.rotules.backend.services.AssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    @Autowired private ChatSessionRepository sessionRepo;
    @Autowired private ChatMessageRepository messageRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private AssistantService assistantService;

//    @PostMapping("/message")
//    public ChatMessage sendMessage(@AuthenticationPrincipal UserDetails userDetails,
//                                   @RequestBody ChatMessageDTO dto) {
//        User user = userRepo.findByUsername(userDetails.getUsername()).orElseThrow();
//
//        // Trouver ou créer une session
//        ChatSession session = sessionRepo.findByUserId(user.getId()).orElseGet(() -> {
//            ChatSession newSession = new ChatSession();
//            newSession.setUser(user);
//            return sessionRepo.save(newSession);
//        });
//
//        // Enregistrer le message de l'utilisateur
//        ChatMessage userMessage = new ChatMessage(session, SenderTypeEnum.USER, dto.getContent(), LocalDateTime.now());
//        messageRepo.save(userMessage);
//
//        // Utiliser l'ID de session comme identifiant pour l'assistant
//        String sessionId = session.getId().toString();
//
//        // Générer la réponse avec l'assistant associé à cette session
//        String botResponse = assistantService.generateResponse(sessionId, dto.getContent());
//
//        // Enregistrer et retourner la réponse
//        ChatMessage botReply = new ChatMessage(session, SenderTypeEnum.BOT, botResponse, LocalDateTime.now());
//        return messageRepo.save(botReply);
//    }

    @PostMapping("/message")
    public ChatMessage sendMessage(@AuthenticationPrincipal UserDetails userDetails,
                                   @RequestBody ChatMessageDTO dto) {
        User user = userRepo.findByUsername(userDetails.getUsername()).orElseThrow();

        // Récupérer ou créer une session utilisateur
        ChatSession session = sessionRepo.findByUserId(user.getId()).orElseGet(() -> {
            ChatSession newSession = new ChatSession();
            newSession.setUser(user);
            return sessionRepo.save(newSession);
        });

        // Enregistrer le message utilisateur
        ChatMessage userMessage = new ChatMessage(session, SenderTypeEnum.USER, dto.getContent(), LocalDateTime.now());
        messageRepo.save(userMessage);

        String botResponse;
        try {
            // Analyser le contenu du message
            String userMessageContent = dto.getContent().toLowerCase();
            StringBuilder contextData = new StringBuilder();

            // 1. Vérification pour les données de vente
            if (userMessageContent.contains("vente") || userMessageContent.contains("ventes") ||
                    userMessageContent.contains("chiffre") || userMessageContent.contains("magasin") ||
                    userMessageContent.contains("revenu") || userMessageContent.contains("données")) {

                // Par défaut, récupérer les 30 derniers jours
                LocalDate endDate = LocalDate.now();
                LocalDate startDate = endDate.minusDays(30);

                // Chercher des mentions de périodes spécifiques
                if (userMessageContent.contains("année") || userMessageContent.contains("an")) {
                    startDate = endDate.minusYears(1);
                } else if (userMessageContent.contains("mois")) {
                    if (userMessageContent.contains("6 mois") || userMessageContent.contains("six mois")) {
                        startDate = endDate.minusMonths(6);
                    } else if (userMessageContent.contains("3 mois") || userMessageContent.contains("trois mois")) {
                        startDate = endDate.minusMonths(3);
                    } else {
                        startDate = endDate.minusMonths(1);
                    }
                } else if (userMessageContent.contains("semaine")) {
                    startDate = endDate.minusWeeks(1);
                }

                // Appel à l'API interne des ventes
                try {
                    String salesEndpoint = "http://localhost:8081/api/v1/data?startDate=" +
                            startDate.toString() + "&endDate=" + endDate.toString();

                    HttpRequest dataRequest = HttpRequest.newBuilder()
                            .uri(URI.create(salesEndpoint))
                            .GET()
                            .build();

                    HttpResponse<String> dataResponse = HttpClient.newHttpClient()
                            .send(dataRequest, HttpResponse.BodyHandlers.ofString());

                    // Ajouter ces données comme contexte
                    contextData.append("Données de vente du ").append(startDate).append(" au ").append(endDate)
                            .append(": ").append(dataResponse.body()).append("\n\n");
                } catch (Exception e) {
                    contextData.append("Impossible d'obtenir les données de vente: ").append(e.getMessage()).append("\n\n");
                }
            }

            // 2. Vérification pour la recherche de produits
            if (userMessageContent.contains("produit") || userMessageContent.contains("article") ||
                    userMessageContent.contains("stock") || userMessageContent.contains("prix")) {

                // Extraire des termes potentiels de recherche
                String searchTerm = extractSearchTerm(userMessageContent);

                if (!searchTerm.isEmpty()) {
                    try {
                        String productsEndpoint = "http://localhost:8081/api/v1/data/products/search?term=" +
                                searchTerm;

                        HttpRequest productRequest = HttpRequest.newBuilder()
                                .uri(URI.create(productsEndpoint))
                                .GET()
                                .build();

                        HttpResponse<String> productResponse = HttpClient.newHttpClient()
                                .send(productRequest, HttpResponse.BodyHandlers.ofString());

                        // Ajouter ces données comme contexte
                        contextData.append("Résultats de recherche de produits pour '").append(searchTerm)
                                .append("': ").append(productResponse.body()).append("\n\n");
                    } catch (Exception e) {
                        contextData.append("Impossible de rechercher des produits: ").append(e.getMessage()).append("\n\n");
                    }
                }
            }

            // Construction du prompt avec les données de contexte
            String systemPrompt = "Réponds de manière concise et naturelle. ";

            if (contextData.length() > 0) {
                systemPrompt += "Analyse attentivement les données fournies et utilise-les pour répondre précisément. " +
                        "Présente les informations importantes de manière claire et structurée. " +
                        "Si des chiffres ou statistiques sont disponibles, mentionne-les.";
            }

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("model", "llama3");

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", contextData.length() == 0 ?
                    dto.getContent() :
                    contextData.toString() + "Question de l'utilisateur: " + dto.getContent()));

            requestMap.put("messages", messages);
            requestMap.put("temperature", 0.7);
            requestMap.put("max_tokens", 500);

            String promptJson = mapper.writeValueAsString(requestMap);

            // Création de la requête HTTP vers llama-server
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(promptJson))
                    .build();

            // Appel du modèle et récupération de la réponse
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            // Extraction du contenu depuis le JSON
            JsonNode json = mapper.readTree(response.body());
            botResponse = json.at("/choices/0/message/content").asText();

            // Nettoyage des artefacts
            botResponse = botResponse
                    .replaceAll("\\|im_start\\|.*?\\n", "")
                    .replaceAll("\\|im_end\\|", "")
                    .trim();

            // Vérification que la dernière phrase est complète
            if (!botResponse.isEmpty() && !botResponse.endsWith(".") && !botResponse.endsWith("!") && !botResponse.endsWith("?")) {
                int lastSentenceEnd = Math.max(
                        Math.max(
                                botResponse.lastIndexOf(". "),
                                botResponse.lastIndexOf("! ")
                        ),
                        botResponse.lastIndexOf("? ")
                );

                if (lastSentenceEnd > 0) {
                    botResponse = botResponse.substring(0, lastSentenceEnd + 1);
                } else {
                    botResponse = botResponse + ".";
                }
            }

        } catch (Exception e) {
            botResponse = "Erreur lors de l'appel à l'IA : " + e.getMessage();
        }

        // Enregistrer la réponse du bot
        ChatMessage botReply = new ChatMessage(session, SenderTypeEnum.BOT, botResponse, LocalDateTime.now());
        return messageRepo.save(botReply);
    }

    // Méthode pour extraire des termes de recherche de produits
    private String extractSearchTerm(String message) {
        // Liste de mots-clés à ignorer dans la recherche
        List<String> stopWords = Arrays.asList("le", "la", "les", "un", "une", "des", "ce", "ces",
                "sur", "avec", "pour", "dans", "par", "produit", "article");

        // Mots à extraire après certains indicateurs
        String[] indicators = {"cherche", "recherche", "trouve", "informations sur", "détails sur",
                "prix de", "stock de", "disponibilité de"};

        for (String indicator : indicators) {
            int index = message.indexOf(indicator);
            if (index >= 0) {
                String afterIndicator = message.substring(index + indicator.length()).trim();
                String[] words = afterIndicator.split("\\s+");
                if (words.length > 0) {
                    // Prendre jusqu'à 3 mots après l'indicateur, en ignorant les stopwords
                    StringBuilder term = new StringBuilder();
                    int count = 0;
                    for (int i = 0; i < Math.min(5, words.length) && count < 3; i++) {
                        String word = words[i].replaceAll("[,.?!]", "").toLowerCase();
                        if (!stopWords.contains(word) && word.length() > 2) {
                            term.append(word).append(" ");
                            count++;
                        }
                    }
                    return term.toString().trim();
                }
            }
        }

        // Si aucun indicateur trouvé, extraire simplement les mots importants
        String[] words = message.split("\\s+");
        StringBuilder term = new StringBuilder();
        int count = 0;

        for (String word : words) {
            word = word.replaceAll("[,.?!]", "").toLowerCase();
            if (!stopWords.contains(word) && word.length() > 3 && count < 2) {
                term.append(word).append(" ");
                count++;
            }
        }

        return term.toString().trim();
    }

    @GetMapping("/messages")
    public List<ChatMessageResponseDTO> getMessages(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepo.findByUsername(userDetails.getUsername()).orElseThrow();
        List<ChatMessage> messages = messageRepo.findAllBySessionUserIdOrderByTimestampAsc(user.getId());
        return messages.stream()
                .map(ChatMessageResponseDTO::fromEntity)
                .toList();
    }

}
