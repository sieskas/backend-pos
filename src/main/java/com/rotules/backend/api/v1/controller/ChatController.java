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
import java.time.LocalDateTime;
import java.util.List;

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
            // Construction du prompt avec une instruction brève
            String promptJson = """
    {
      "model": "llama3",
      "messages": [
        {"role": "system", "content": "Réponds de manière concise et naturelle. Évite de terminer tes réponses par des points de suspension."},
        {"role": "user", "content": "%s"}
      ],
      "temperature": 0.7,
      "max_tokens": 500
    }
    """.formatted(dto.getContent().replace("\"", "\\\""));

            // Création de la requête HTTP vers llama-server
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(promptJson))
                    .build();

            // Appel du modèle et récupération de la réponse
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            // Extraction propre du contenu depuis le JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.body());
            botResponse = json.at("/choices/0/message/content").asText();

            // Nettoyage des éventuels artefacts
            botResponse = botResponse
                    .replaceAll("\\|im_start\\|.*?\\n", "")
                    .replaceAll("\\|im_end\\|", "")
                    .trim();

            // Suppression des points de suspension à la fin si présents
            if (botResponse.endsWith("...")) {
                botResponse = botResponse.substring(0, botResponse.length() - 3).trim();
            }

        } catch (Exception e) {
            botResponse = "Erreur lors de l'appel à l'IA : " + e.getMessage();
        }

        // Enregistrer la réponse du bot
        ChatMessage botReply = new ChatMessage(session, SenderTypeEnum.BOT, botResponse, LocalDateTime.now());
        return messageRepo.save(botReply);
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
