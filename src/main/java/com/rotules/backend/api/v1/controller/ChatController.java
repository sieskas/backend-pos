package com.rotules.backend.api.v1.controller;

import com.rotules.backend.api.v1.controller.resources.ChatMessageDTO;
import com.rotules.backend.api.v1.controller.resources.ChatMessageResponseDTO;
import com.rotules.backend.domain.ChatMessage;
import com.rotules.backend.domain.ChatSession;
import com.rotules.backend.domain.SenderTypeEnum;
import com.rotules.backend.domain.User;
import com.rotules.backend.outcall.db.repository.ChatMessageRepository;
import com.rotules.backend.outcall.db.repository.ChatSessionRepository;
import com.rotules.backend.outcall.db.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    @Autowired private ChatSessionRepository sessionRepo;
    @Autowired private ChatMessageRepository messageRepo;
    @Autowired private UserRepository userRepo;

    @PostMapping("/message")
    public ChatMessage sendMessage(@AuthenticationPrincipal UserDetails userDetails, @RequestBody ChatMessageDTO dto) {
        User user = userRepo.findByUsername(userDetails.getUsername()).orElseThrow();
        ChatSession session = sessionRepo.findByUserId(user.getId()).orElseGet(() -> {
            ChatSession newSession = new ChatSession();
            newSession.setUser(user);
            return sessionRepo.save(newSession);
        });

        ChatMessage userMessage = new ChatMessage(session, SenderTypeEnum.USER, dto.getContent(), LocalDateTime.now());
        messageRepo.save(userMessage);

        // Réponse du bot
        ChatMessage botReply = new ChatMessage(session, SenderTypeEnum.BOT, "Bonjour !", LocalDateTime.now());
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
