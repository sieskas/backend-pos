package com.rotules.backend.api.v1.controller.resources;


import com.rotules.backend.domain.SenderTypeEnum;

public class ChatMessageDTO {

    private String content;
    private SenderTypeEnum sender;

    public ChatMessageDTO() {}

    public ChatMessageDTO(String content, SenderTypeEnum sender) {
        this.content = content;
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public SenderTypeEnum getSender() {
        return sender;
    }

    public void setSender(SenderTypeEnum sender) {
        this.sender = sender;
    }
}
