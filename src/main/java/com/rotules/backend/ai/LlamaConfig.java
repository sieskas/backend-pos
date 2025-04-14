package com.rotules.backend.ai;

import com.rotules.backend.services.AssistantService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LlamaConfig {

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return new LlamaCppChatModel("http://localhost:8080");
    }
}