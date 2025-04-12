package com.rotules.backend.api.v1.controller.resources;

import com.rotules.backend.domain.ChatMessage;
import com.rotules.backend.domain.SenderTypeEnum;

import java.time.LocalDateTime;

public class ChatMessageResponseDTO {

    private String content;
    private SenderTypeEnum sender;
    private LocalDateTime timestamp;

    public ChatMessageResponseDTO() {}

    public ChatMessageResponseDTO(String content, SenderTypeEnum sender, LocalDateTime timestamp) {
        this.content = content;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    public static ChatMessageResponseDTO fromEntity(ChatMessage msg) {
        return new ChatMessageResponseDTO(
                msg.getContent(),
                msg.getSender(),
                msg.getTimestamp()
        );
    }

    public String getContent() {
        return content;
    }

    public SenderTypeEnum getSender() {
        return sender;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSender(SenderTypeEnum sender) {
        this.sender = sender;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
